# 실행 검증 기록

검증 일자: 2026-11-09, Windows PowerShell, Asia/Seoul. 실제 명령을 실행한 결과만 기록합니다. 로컬 도구·로그·DB·JAR는 `.tools/`, `.m2/`, `data/`, `target/` 아래 있으며 Git 제외 대상입니다.

## 환경과 고정 버전

- JDK: `C:/Program Files/Java/jdk-17/bin/java.exe`, Java **17.0.12**
- Spring Boot **3.5.16**, Maven **3.9.11**, Maven Wrapper **3.3.4**
- 실행 중 확인된 Hibernate **6.6.53.Final**, H2 **2.3.232**
- Wrapper ZIP SHA-256: `0d7125e8c91097b36edb990ea5934e6c68b4440eef4ea96510a0f6815e7eeadb`
- `mvnw`의 CR 바이트 수 **0**: Unix LF 확인. Windows에서 Git 실행 비트를 지정하지 않았으므로 README에 `chmod +x mvnw`를 명시했습니다.

일반 샌드박스에서는 Maven Central DNS 조회가 실패했습니다. 해당 프로젝트의 공개 빌드 의존성 다운로드와 Java 실행만 권한 확장하여 수행했습니다. 시스템 Java 설정이나 PATH를 수정하지 않고 실제 JDK 경로를 사용했습니다.

## 테스트 먼저 실행한 근거

1. API가 없는 부트스트랩 상태에서 `ClinicApiTest`를 먼저 실행했습니다. **5개 테스트 실패, 오류 0개**. 생성 요청의 예상 `201`/검증 요청의 예상 `400`에 실제 `404`가 반환되는 실패를 확인한 뒤 Controller·Service·Repository·도메인을 구현했습니다.
2. 첫 구현 실행은 **5개 중 4개 통과**. Spring MVC 메서드 검증의 `ParameterErrors`가 DTO의 필드명을 잃는 문제를 확인했습니다. 오류 처리기가 DTO 필드별 오류를 유지하도록 고쳤습니다.
3. 시드 구현 전 `SeedIntegrationTest`는 보호자 **3명 예상 / 실제 0명**으로 실패했습니다. 이후 시드 CLI를 구현했습니다.
4. 허용된 중복 보호자가 있을 때 시드 재실행 회귀 테스트는 `NonUniqueResultException`으로 실패했습니다. 중복 자연키가 있으면 ID가 가장 작은 행을 선택하도록 조회를 수정했습니다. 중복 반려동물도 함께 확인합니다.

## 전체 빌드

프로젝트 디렉터리에서 아래 명령을 실행했습니다. 이 환경에서는 캐시를 프로젝트 안으로 한정했습니다. 일반 개발 시 캐시 변수와 `maven.repo.local` 옵션은 생략할 수 있습니다.

```powershell
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-17'
$env:MAVEN_USER_HOME = (Join-Path (Get-Location) '.tools/maven-home')
.\mvnw.cmd '-Dmaven.repo.local=.m2' -B -ntp clean verify
```

초기 완성본은 **6개 테스트 / 실패 0 / 오류 0**, 실행 가능한 Spring Boot JAR 생성 성공을 확인했습니다. 루트 검토자도 별도로 Wrapper 테스트를 실행해 같은 결과를 확인했습니다. 이후 중복 시드 회귀 테스트를 추가했습니다.

새 빌드 중 HTTP 검증 서버가 이전 JAR를 연 상태라 Windows 파일 잠금으로 `repackage`가 한 번 실패했습니다. 그 실행의 **7개 테스트는 모두 통과**했습니다. 직접 시작한 clinic Java 프로세스의 명령행을 확인한 뒤 종료하고 `clean verify`를 다시 수행했습니다. 최종 결과는 아래 기록을 따릅니다.

최종 실행(22:36:32 KST): **`BUILD SUCCESS`**, `Tests run: 7, Failures: 0, Errors: 0, Skipped: 0`, 종료 코드 **0**. `ClinicApiTest` 5개와 `SeedIntegrationTest` 2개를 실행했고 `target/devon-demo-clinic-1.0.0.jar`를 다시 패키징했습니다. 실행 시간은 Maven 출력 기준 **25.530초**입니다.

통합 테스트는 실제 Spring 애플리케이션·MockMvc·JPA·H2를 사용합니다. 검증 내용은 보호자/반려동물 CRUD, 이름 검색·페이지, 빈 결과, 관계 ID 불일치, 없는 리소스, 필수값·미래 날짜·잘못된 JSON, 생일 이전 진료 거절, 기존 진료보다 뒤로 생일을 바꾸지 못하는 정책, 두 가지 부모 삭제 제한, 반복 시드와 중복 이름 처리입니다.

## 빈 파일 DB와 반복 시드

기존 파일이 없는 `data/verification-20261109` 경로를 먼저 확인하고 실행했습니다.

```powershell
$env:DB_URL = 'jdbc:h2:file:./data/verification-20261109;DB_CLOSE_ON_EXIT=FALSE'
& "$env:JAVA_HOME/bin/java.exe" -jar target/devon-demo-clinic-1.0.0.jar --spring.profiles.active=init-db
& "$env:JAVA_HOME/bin/java.exe" -jar target/devon-demo-clinic-1.0.0.jar --spring.profiles.active=seed
& "$env:JAVA_HOME/bin/java.exe" -jar target/devon-demo-clinic-1.0.0.jar --spring.profiles.active=seed
```

초기화 직후와 시드 두 번 실행 후 H2 Shell로 실제 DB의 `COUNT(*)`를 조회했습니다.

| 시점 | owners | pets | visits | 결과 |
| --- | ---: | ---: | ---: | --- |
| 새 DB 초기화 후 | 0 | 0 | 0 | CLI 종료 코드 0 |
| 첫 시드 후 | 3 | 4 | 5 | `SEED complete` 로그, 종료 코드 0 |
| 두 번째 시드 후 | 3 | 4 | 5 | 로그와 SQL 조회 일치, 종료 코드 0 |

조회 SQL:

```sql
SELECT (SELECT COUNT(*) FROM owners) AS owners,
       (SELECT COUNT(*) FROM pets) AS pets,
       (SELECT COUNT(*) FROM visits) AS visits;
```

## 실제 HTTP 요청

동일한 H2 파일 DB로 JAR를 실행해 Tomcat **8081** 기동을 확인했습니다. 그 후 다음 명령을 실제 실행했습니다.

```powershell
.\scripts\smoke.ps1 -BaseUrl http://localhost:8081
```

실제 출력:

```text
PASS owner=4 pet=5 visit=6: creation, lists, pagination, 409 restrict, 400 validation
```

생성 응답 ID를 사용한 보호자 → 반려동물 → 진료 생성, 진료 목록, 보호자 검색·페이지, 진료가 있는 반려동물 삭제의 409와 데이터 유지, 잘못된 보호자 본문의 400을 확인했습니다.

`GET /api/owners?name=%EA%B9%80&page=0&size=1`은 HTTP **200**, `김민지`, `totalElements: 1`을 반환했습니다. 검토자도 별도 HTTP 요청으로 시드 데이터와 없는 보호자 404를 확인했습니다. 검증 서버는 작업 후 종료했습니다.

## 범위와 남은 한계

- 실제로 실행한 플랫폼은 Windows입니다. Linux/macOS 명령은 표준 Java/Maven 실행법이며 별도 OS 런타임 테스트는 하지 않았습니다.
- H2와 로컬 HTTP를 검증했습니다. Docker, 운영 DB, 배포, 인증, 부하 테스트는 구현 범위에 없습니다.
- 외부 유료 API와 인증 키를 사용하지 않았습니다. DB/로그/다운로드 캐시는 소스와 분리했습니다.
- DevOn 원본 파일 변경, Git 초기화·커밋·푸시는 수행하지 않았습니다. GitHub 게시 이후 분석·매칭·면접 시나리오는 [devon-testing.md](devon-testing.md)에 분리했습니다.
