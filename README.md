# DevOn Clinic Demo

DevOn의 GitHub 저장소 분석·채용 공고 매칭·코드 기반 모의 면접을 테스트하는 데모 프로젝트입니다. 보호자 → 반려동물 → 진료 기록을 관리하는 독립적인 Spring REST API 예제입니다. 학습·포트폴리오·API 연동 실습을 위한 프로젝트이며 기존 DevOn 서비스와 연결되지 않습니다. 실제 보호자 개인정보나 진료 정보를 넣지 마세요.

DevOn 등록, 저장소 분석 키워드, 합성 채용 공고 매칭, 모의 면접 시나리오는 [docs/devon-testing.md](docs/devon-testing.md)를 참고하세요.

## 구현 범위와 선택

- 보호자 생성·단건 조회·수정·삭제, 이름 부분 검색과 페이지 조회
- 보호자에 속한 반려동물 생성·목록·단건 조회·수정·삭제
- 반려동물의 진료 기록 생성·목록 조회, 날짜 내림차순 정렬
- Bean Validation, 일관된 JSON 오류, 트랜잭션과 외래키 제약
- 빈 DB 초기화, 반복 실행 가능한 예시 데이터, 실제 Spring/H2 통합 테스트

Java 17, Spring Boot **3.5.16**, Maven Wrapper **3.3.4** / Maven **3.9.11**을 고정했습니다. JPA·Validation·H2·JUnit 버전은 해당 Spring Boot BOM이 고정합니다. 데이터베이스 서버 설치 없이 실행할 수 있도록 H2 파일 DB를 사용합니다. 운영 DB 마이그레이션 도구 대신 명시적인 `schema.sql`과 Hibernate `validate`로 이 작은 예제의 테이블 정의를 확인합니다.

## 구조와 요청 흐름

```text
src/main/java/dev/devon/clinic/
  ClinicApplication.java      # 웹 실행 / 초기화·시드 CLI 종료
  api/                        # Controller, 요청·응답 DTO, 오류 처리
  service/                    # 트랜잭션, 관계·날짜 정책, 시드
  repository/                 # Spring Data JPA 조회
  domain/                     # Owner, Pet, Visit JPA 엔티티
src/main/resources/
  application*.yml            # 기본/seed/init-db 프로필
  schema.sql                  # 빈 DB 생성과 FK RESTRICT
src/test/java/dev/devon/clinic/ # 실제 Spring + H2 + MockMvc 통합 테스트
docs/verification.md           # 실제 실행 결과
docs/publication.md            # GitHub 게시와 API 수집 조건 판정
scripts/smoke.ps1              # 실행 중인 HTTP 서버 확인
```

HTTP → Controller에서 JSON·경로·쿼리 검증 → Service에서 관계 확인과 트랜잭션 → Repository → H2 순서입니다. 응답은 DTO로 변환하므로 지연 로딩 엔티티나 순환 참조가 JSON에 노출되지 않습니다. 목록 응답도 트랜잭션 안에서 변환합니다.

`Owner 1 ── N Pet 1 ── N Visit` 관계이며 삭제 전 자동으로 자식을 지우지 않습니다. 반려동물이 남은 보호자, 진료 기록이 남은 반려동물을 삭제하면 **409**를 반환합니다. DB 외래키도 RESTRICT를 적용하므로 동시에 자식이 추가되더라도 잘못된 부모 삭제를 차단합니다. 진료 기록 삭제·수정은 이 예제 범위에 없습니다.

## 설치

필수: **JDK 17 이상**, 인터넷 연결. Maven은 Wrapper가 내려받습니다. Node, Docker, 별도 H2 설치는 필요 없습니다.

PowerShell:

```powershell
cd devon-demo-clinic
# 자신의 실제 JDK 설치 경로를 사용합니다.
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-17'
& "$env:JAVA_HOME/bin/java.exe" -version
.\mvnw.cmd -B -ntp clean verify
```

macOS / Linux:

```sh
cd devon-demo-clinic
export JAVA_HOME=/path/to/jdk-17
chmod +x mvnw
./mvnw -B -ntp clean verify
```

Wrapper는 Maven Central의 고정 ZIP과 SHA-256을 사용합니다. 첫 실행에는 의존성 다운로드가 필요합니다. 사내 프록시 환경에서는 Maven의 `settings.xml`을 사용하세요. Java 실행이 멈추는 Windows 환경에서는 Oracle `javapath` 바로가기 대신 실제 JDK의 `JAVA_HOME`을 지정하세요.

## 환경 변수

`.env.example`은 변수 목록 예시이며 **Spring이 `.env`를 자동으로 읽지는 않습니다**. 셸에 직접 설정하세요.

| 변수 | 기본값 | 설명 |
| --- | --- | --- |
| `PORT` | `8081` | HTTP 포트 |
| `DB_URL` | `jdbc:h2:file:./data/clinic;DB_CLOSE_ON_EXIT=FALSE` | 프로젝트 실행 위치 기준 H2 파일 |
| `DB_USERNAME` | `sa` | 로컬 예제 사용자 |
| `DB_PASSWORD` | 빈 문자열 | 로컬 예제 비밀번호 |

```powershell
$env:PORT = '8081'
$env:DB_URL = 'jdbc:h2:file:./data/clinic;DB_CLOSE_ON_EXIT=FALSE'
```

`data/`, `target/`, `.m2/`, `.tools/`, `.env`는 Git에서 제외합니다. DB와 빌드 산출물, 개발 도구, 비밀 값은 저장소에 올리지 않습니다. 로컬 H2 콘솔은 기본 비활성화입니다.

## DB 초기화와 시드

모든 명령은 프로젝트 디렉터리에서 실행합니다. 서버가 같은 DB를 열고 있다면 먼저 종료하세요. H2 파일은 한 프로세스에서 사용합니다.

```powershell
# 빈 파일 DB의 명시적 초기화: 테이블 생성 + 엔티티/스키마 검증 후 종료
& "$env:JAVA_HOME/bin/java.exe" -jar target/devon-demo-clinic-1.0.0.jar --spring.profiles.active=init-db

# 예시 보호자 3명, 반려동물 4마리, 진료 5건 추가 후 종료
& "$env:JAVA_HOME/bin/java.exe" -jar target/devon-demo-clinic-1.0.0.jar --spring.profiles.active=seed

# 반복해도 동일한 예시 행을 추가하지 않음
& "$env:JAVA_HOME/bin/java.exe" -jar target/devon-demo-clinic-1.0.0.jar --spring.profiles.active=seed
```

Unix에서는 `"$JAVA_HOME/bin/java" -jar ...`로 같은 인자를 사용합니다. `init-db`/`seed`는 웹 포트를 열지 않는 CLI 프로필입니다. `schema.sql`은 `CREATE ... IF NOT EXISTS`를 사용하며 기존 데이터를 삭제하지 않습니다. 기본 웹 실행에서도 빈 DB 테이블은 자동 생성하지만 예시 행은 추가하지 않습니다.

시드는 보호자 `(name, telephone)`, 반려동물 `(ownerId, name)`, 진료 `(petId, visitDate, description)`를 확인하여 누락된 샘플만 채웁니다. 동일한 키가 여러 개면 가장 먼저 생성된 행을 선택합니다. 샘플 이름·전화번호를 수정하면 새 샘플로 다시 생성될 수 있습니다. 동시 시드 실행은 지원하지 않습니다. 예시 데이터는 정기 예방접종, 치과 검진, 발바닥 상처, 피부 진료, 토끼 건강검진의 관계를 보여줍니다.

완전히 새로운 DB로 확인하려면 기존 파일을 지우는 대신 별도 경로를 지정하세요.

```powershell
$env:DB_URL = 'jdbc:h2:file:./data/new-demo;DB_CLOSE_ON_EXIT=FALSE'
& "$env:JAVA_HOME/bin/java.exe" -jar target/devon-demo-clinic-1.0.0.jar --spring.profiles.active=init-db
```

## 서버 실행

```powershell
& "$env:JAVA_HOME/bin/java.exe" -jar target/devon-demo-clinic-1.0.0.jar
# 개발 중 대안
.\mvnw.cmd spring-boot:run
```

기본 주소는 `http://localhost:8081`입니다. 종료는 `Ctrl+C`입니다. `GET /api/owners`가 JSON 페이지를 반환하면 실행 중입니다. 기본적으로 빈 DB이므로 시드를 실행하지 않았다면 `content: []`가 정상입니다.

## API 계약

모든 요청 본문은 `Content-Type: application/json`입니다. 생성은 **201**과 `Location`, 수정·조회는 **200**, 삭제는 본문 없는 **204**입니다. PUT은 아래 필드를 전부 받는 전체 수정이며 ID·소유자 변경 필드는 받지 않습니다. 모르는 JSON 필드도 **400**입니다.

| 메서드 | 경로 | 동작 |
| --- | --- | --- |
| GET / POST | `/api/owners` | 검색·페이지 목록 / 생성 |
| GET / PUT / DELETE | `/api/owners/{ownerId}` | 보호자 조회 / 수정 / 삭제 |
| GET / POST | `/api/owners/{ownerId}/pets` | 반려동물 목록 / 생성 |
| GET / PUT / DELETE | `/api/owners/{ownerId}/pets/{petId}` | 반려동물 조회 / 수정 / 삭제 |
| GET / POST | `/api/owners/{ownerId}/pets/{petId}/visits` | 진료 목록 / 생성 |

`GET /api/owners?name=kim&page=0&size=20`: 이름 부분 일치, 대소문자 무시, ID 오름차순. `page`는 0 이상, `size`는 1~100, 기본 20입니다. 응답은 `content`, `page`, `size`, `totalElements`, `totalPages`를 갖습니다. 검색어 최대 100자이며 `%`와 `_`도 문자로 검색합니다. 반려동물은 ID 오름차순, 진료는 `visitDate DESC, id DESC`입니다.

보호자 입력: `name` 필수 최대 100자, `telephone` 필수 최대 30자, `address` 필수 최대 255자. 반려동물: `name` 필수 최대 100자, `type` 필수 최대 50자, `birthDate` 필수 `YYYY-MM-DD`. 종류는 `cat`, `dog`, `rabbit` 등 자유 문자열입니다. 진료: `visitDate` 필수 `YYYY-MM-DD`, `description` 필수 최대 1000자. 공백만 있는 문자열은 거절하고 저장할 때 앞뒤 공백을 제거합니다.

생일과 진료일은 서버의 오늘까지 허용합니다. 생일 이전 진료를 만들 수 없고 기존 진료일 이후로 생일을 바꿀 수도 없습니다. 중첩 경로의 보호자와 반려동물 관계가 다르거나 리소스가 없으면 **404**입니다. 양수가 아닌 ID나 잘못된 쿼리는 **400**입니다.

## 바로 실행하는 예제

PowerShell은 JSON을 UTF-8로 보내기 위해 바이트 변환을 사용합니다. 응답의 ID를 다음 요청에 전달하므로 시드 ID를 가정하지 않습니다.

```powershell
$base = 'http://localhost:8081'
$ownerBody = @{ name='Demo Owner'; telephone='010-5555-1234'; address='Seoul' } | ConvertTo-Json
$owner = Invoke-RestMethod "$base/api/owners" -Method Post -ContentType 'application/json' -Body ([Text.Encoding]::UTF8.GetBytes($ownerBody))
$petBody = @{ name='Nabi'; type='cat'; birthDate='2020-04-01' } | ConvertTo-Json
$pet = Invoke-RestMethod "$base/api/owners/$($owner.id)/pets" -Method Post -ContentType 'application/json' -Body ([Text.Encoding]::UTF8.GetBytes($petBody))
$visitBody = @{ visitDate='2024-03-10'; description='Annual vaccination' } | ConvertTo-Json
Invoke-RestMethod "$base/api/owners/$($owner.id)/pets/$($pet.id)/visits" -Method Post -ContentType 'application/json' -Body ([Text.Encoding]::UTF8.GetBytes($visitBody))
Invoke-RestMethod "$base/api/owners?name=Demo&page=0&size=10"
Invoke-RestMethod "$base/api/owners/$($owner.id)/pets/$($pet.id)/visits"
```

curl 예시(생성 응답의 실제 ID를 사용):

```sh
curl -i -X POST http://localhost:8081/api/owners \
  -H 'Content-Type: application/json' \
  -d '{"name":"Demo Owner","telephone":"010-5555-1234","address":"Seoul"}'
curl 'http://localhost:8081/api/owners?name=Demo&page=0&size=10'
curl -i -X PUT http://localhost:8081/api/owners/1 \
  -H 'Content-Type: application/json' \
  -d '{"name":"Demo Updated","telephone":"010-5555-1234","address":"Busan"}'
```

관계 충돌 예시: 진료가 있는 `DELETE /api/owners/1/pets/1`은 409이며 행을 유지합니다. 비어 있는 보호자는 삭제할 수 있고, 진료가 없는 반려동물도 삭제할 수 있습니다.

오류 응답은 동일한 형태입니다. 필드별 검증 외 오류에서는 `fields`가 빈 객체입니다. 내부 예외·SQL·스택은 클라이언트에 공개하지 않습니다.

```json
{
  "timestamp": "2026-11-09T13:30:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/owners",
  "fields": {"name": "must not be blank"}
}
```

주요 코드: `VALIDATION_ERROR` 400, `INVALID_REQUEST` 400, `NOT_FOUND` 404, `RELATIONSHIP_CONFLICT` 409, `METHOD_NOT_ALLOWED` 405, `UNSUPPORTED_MEDIA_TYPE` 415, `INTERNAL_ERROR` 500.

## 테스트와 확인

```powershell
.\mvnw.cmd -B -ntp clean verify
# 서버 실행 후 실제 HTTP 생성·조회·오류·삭제 정책 확인
.\scripts\smoke.ps1 -BaseUrl http://localhost:8081
```

테스트는 별도 인메모리 H2를 사용해 로컬 파일 DB에 영향을 주지 않습니다. MockMvc는 실제 Controller·Service·Repository와 관계 제약을 실행하며 저장소를 모킹하지 않습니다. CRUD, 검색·페이지, 중첩 ID 불일치, 필수값·미래일·생일 이후 변경, 없는 리소스, 삭제 제한과 시드 반복을 확인합니다. HTTP smoke는 실행 중인 DB에 새 보호자·반려동물·진료를 추가합니다. 진료 삭제 API가 없어 smoke 행은 남습니다. 실제 실행 명령·결과·환경은 [docs/verification.md](docs/verification.md)에 기록합니다.

## 한계와 확장 지점

- API 예제이므로 화면·인증·권한·예약·수의사·처방·결제는 없습니다. 인증이 없으므로 누구나 데이터를 바꿀 수 있어 로컬 학습용으로 사용합니다.
- H2 단일 프로세스 파일 DB, 동시 시드·운영 마이그레이션·백업·다중 인스턴스는 지원하지 않습니다.
- 반려동물/진료 목록은 이 예제에서 페이지를 나누지 않습니다. 대량 데이터라면 페이지 API와 검색 인덱스가 필요합니다.
- 중복 보호자·반려동물 이름과 같은 날짜의 여러 진료는 허용합니다. 시드 판별 키는 일반 사용자 데이터의 고유 제약이 아닙니다.
- 기존 스키마의 버전 업그레이드는 자동 수행하지 않습니다. `validate` 실패 시 DB를 임의로 덮어쓰지 말고 명시적 마이그레이션을 추가하세요.

## 참고와 라이선스

[Spring Petclinic](https://github.com/spring-projects/spring-petclinic)의 보호자/반려동물/진료 도메인과 Maven Wrapper 구성을 참고했습니다. 애플리케이션 Java 코드와 예시 데이터는 새로 작성했으며 원본 UI·이미지·자바 코드를 복사하지 않았습니다. 표준 Maven Wrapper 스크립트의 Apache-2.0 헤더는 보존했습니다. 생성 경로·출처·외부 라이선스는 [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md), 전문은 [licenses/Apache-2.0.txt](licenses/Apache-2.0.txt)에서 확인합니다.

이 프로젝트의 자체 코드는 [Apache-2.0](LICENSE) 라이선스로 제공합니다.

[Spring Boot 3.5 시스템 요구사항](https://docs.spring.io/spring-boot/3.5/system-requirements.html), [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/), [Maven Wrapper](https://maven.apache.org/wrapper/), [H2 문서](https://h2database.com/html/main.html)를 사용했습니다.
