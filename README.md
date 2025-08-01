# Hwaroak Server Repository

## Tech Stack
- Spring Boot 3.5.3
- MySQL 8.0.33
- 배포 환경 : 미정

---

## Branch Convention

| Branch    | 설명                                                       |
|-----------|----------------------------------------------------------|
| `main`    | 실제 배포(CI/CD)를 위한 브랜치입니다.                                 |
| `develop` | 실제 배포 전 기능을 개발하는 브랜치입니다.                                 |
| `feature`   | 새로운 기능 구현을 위한 브랜치<br>`develop`에서 분기 → `develop`으로 병합합니다. |
| `fix`     | 배포 전 기능 수정용 브랜치입니다.                                      |
| `hotfix`  | 운영 중인 서비스에서 발생한 긴급 버그 수정용 브랜치입니다.                        |

> 모든 브랜치는 명확한 목적에 맞게 사용하며, 적절한 브랜치로 병합되어야 합니다.
> 
| **예시: feat/13-kakao-login**

---

## Pull Request Convention

`[<Prefix>] <Description>`의 양식을 준수하되, prefix는 commit message convention을 따릅니다.

| **예시: [feat] 카카오 로그인 구현**

- PR 생성 시 24시간 이내에 확인을 요합니다.
- `develop` 브랜치로의 병합은 **최소 1명 이상의 리뷰어 승인(Approve)** 이 필요합니다.

---

## Commit Convention

`<Prefix>: <Description> (#<Issue_Number>)` 의 양식을 준수합니다.
- **feat**: 새로운 기능 구현`feat: 구글 로그인 API 기능 구현 (#11)`
- **fix**: 코드 오류 수정`fix: 회원가입 비즈니스 로직 오류 수정 (#10)`
- **del**: 불필요한 코드 삭제`del: 불필요한 import 제거 (#12)`
- **docs**: README나 wiki 등의 문서 개정`docs: 리드미 수정 (#14)`
- **refactor**: 내부 로직은 변경 하지 않고 기존의 코드를 개선하는 리팩터링`refactor: 코드 로직 개선 (#15)`
- **chore**: 의존성 추가, yml 추가와 수정, 패키지 구조 변경, 파일 이동 등의 작업 `chore: yml 수정 (#21)`, `chore: lombok 의존성 추가 (#22)`
- **test**: 테스트 코드 작성, 수정 `test: 로그인 API 테스트 코드 작성 (#20)`
---

## Package Convention (Domain Driven Design)

전통적인 **계층형 아키텍처(Layered Architecture)** 구조를 기반으로 패키지를 구성합니다.
```dockerignore
🔥hwaroak
 ┣ 📂.github
 ┃ ┣ 📂ISSUE_TEMPLATE   # 이슈 템플릿
 ┃ ┣ 📂workflows     # Github Actions Workflow 설정
 ┣ 📂nginx       # Proxy 설정
 ┃ ┣ 📂html
 ┣ 📂src
 ┃ ┗ 📂main
 ┃   ┣ 📂java
 ┃   ┃ ┗ 📂com
 ┃   ┃   ┗ 📂umc
 ┃   ┃     ┗ 📂hwaroak
 ┃   ┃       ┣ 📂service   # 도메인별 핵심 비즈니스 로직 영역
 ┃   ┃       ┣ 📂controller  # 도메인별 핵심 비즈니스 로직 영역
 ┃   ┃       ┣ 📂config   # 프로젝트 설정 파일들(Security, Web 등)
 ┃   ┃       ┣ 📂converter  # Entity <-> DTO 간 변환
 ┃   ┃       ┣ 📂domain    # 전역적으로 사용하는 domain
 ┃   ┃       ┃ ┣ 📂entity    # 프로젝트 Entity(JPA로 DB와 매핑되는 크래스들)
 ┃   ┃       ┣ 📂dto   # 클라이언트와 데이터를 주고받을 객체
 ┃   ┃       ┃ ┣ 📂request   # 요청 객체
 ┃   ┃       ┃ ┗ 📂response   # 응답 객체
 ┃   ┃       ┣ 📂exception  # 전역 예외 처리
 ┃   ┃       ┣ 📂repository   # JPA Repository 인터페이스
 ┃   ┃       ┗ 📂util   # 공통 유틸리티 클래스 정의
 ┃   ┃       ┗ 📜HwaroakApplication.java # Spring Boot 메인 실행 클래스
 ┃   ┗ 📂resources
 ┃     ┣ 📂static
 ┃     ┣ 📜application-dev.yaml # 개발 프로필
 ┃     ┣ 📜application-local.yaml # 로컬 프로필
 ┃     ┗ 📜application.yaml # 공통 프로필
 ┣ 📜.env # 중요 환경변수 설정
 ┣ 📜.gitignore
 ┣ 📜README.md
 ┗ 📜build.gradle
```

> 기능 단위로가 아닌 역할에 따른 계층 분리를 통해 각 레이어의 책임을 명확히 합니다.
>
---
