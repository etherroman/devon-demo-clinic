# DevOn 분석·공고 매칭·면접 테스트

이 문서는 이 저장소로 DevOn의 분석 결과와 질문 품질을 확인하기 위한 시나리오다. 아래 공고는 테스트용 가상 공고이며 실제 채용 정보가 아니다.

## 분석 키워드

Java, Spring Boot, Spring Data JPA, Hibernate, Bean Validation, H2, Maven Wrapper, REST API, 트랜잭션, 엔티티 관계, 통합 테스트

저장소 분석 후 사용 기술의 존재 여부뿐 아니라 인증/검증/데이터 접근/테스트의 실제 코드 경로를 근거로 제시하는지 확인한다. README의 기술 목록만 반복하거나 구현하지 않은 기술을 추정하는지도 점검한다.

## 공고 매칭 입력 예시

> Java/Spring Boot와 Spring Data JPA로 도메인 중심 REST API를 개발하는 백엔드 엔지니어를 찾습니다. Bean Validation, 연관관계 설계, 트랜잭션, 일관된 예외 응답, 데이터 접근 통합 테스트 경험을 확인합니다.

평가 시 일치 기술의 코드 근거, 구현 범위, 공고 요구사항 대비 확인할 수 없는 항목을 구분한다. 배포 운영 경험이나 실제 팀 협업 이력은 이 데모만으로 입증되지 않는다.

## 코드 기반 질문 주제

1. 보호자–반려동물–진료 기록의 외래키와 JPA 연관관계를 어떻게 구성했나요?
2. 연관 데이터가 있는 보호자나 반려동물 삭제를 거부하는 이유와 409 응답이 만들어지는 경로를 설명해 주세요.
3. 중첩 URL의 보호자와 실제 반려동물의 보호자가 다르면 어떻게 처리하나요?
4. 서비스의 트랜잭션 경계는 어디이며, 조회와 저장에서 영속성 컨텍스트가 어떤 역할을 하나요?
5. 미래 생일·진료일, 빈 이름, 존재하지 않는 ID, 잘못된 페이지 값은 어떤 계층에서 검증하나요?
6. H2로 통과한 테스트를 PostgreSQL 운영 환경에 그대로 일반화할 수 없는 이유는 무엇인가요?

면접 답변은 설명과 해당 코드/테스트 위치를 함께 확인한다. 정답 문자열 일치보다 구현상의 선택과 한계를 설명하는지 평가한다.

## 독립 Public 저장소 게시

아래 명령은 이 프로젝트 디렉터리에서 PowerShell로 실행한다. 이 작업에서는 GitHub 저장소 생성이나 push를 수행하지 않았다. 테스트 계정으로 GitHub CLI에 로그인한 뒤 `gh api user --jq .login`으로 소유자를 확인한다. 서비스 로그인 이메일/비밀번호를 소스 파일이나 Git 명령에 넣지 않는다.

~~~powershell
gh auth login --hostname github.com --web
$demoOwner = gh api user --jq .login
git init -b main
git add .
git diff --cached --stat
git status --short
# .env, DB, 의존성 설치 폴더, 빌드 산출물이 포함되지 않았는지 확인 후:
git commit -m "feat: add devon-demo-clinic"
gh repo create "$demoOwner/devon-demo-clinic" --public --source . --remote origin --push
~~~

원본 저장소의 fork를 만들지 않고 새 저장소를 생성하는 명령이다. 기존에 같은 이름의 저장소가 있으면 명령이 실패하므로 덮어쓰거나 강제 push하지 않는다.

## 게시 후 실제 수집 조건 확인

~~~powershell
$demoOwner = gh api user --jq .login
gh api "repos/$demoOwner/devon-demo-clinic" --jq '{full_name,private,fork,archived,language,size}'
gh api "repos/$demoOwner/devon-demo-clinic" --jq '(.private == false) and (.fork == false) and (.archived == false) and (.language != null) and (.size >= 50)'
~~~

두 번째 명령의 결과가 `true`인지 확인한다. 언어 집계와 저장소 메타데이터가 갱신된 뒤 재확인할 수 있다. 여기서 `size`는 GitHub API가 반환하는 값이다. 로컬 디렉터리의 파일 크기 합계나 압축 파일 크기로 대신 판정하지 않는다. 게시 전에는 `size >= 50` 충족을 보장할 수 없다. 더미 파일이나 생성 산출물을 추가해 크기를 부풀리지 않는다.

공식 사용법: [gh repo create](https://cli.github.com/manual/gh_repo_create), [GitHub 저장소 REST API](https://docs.github.com/en/rest/repos/repos#get-a-repository).

