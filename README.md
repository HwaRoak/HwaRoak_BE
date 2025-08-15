# 🔥 화록 서버 README

<img width="2048" height="1365" alt="image" src="https://github.com/user-attachments/assets/a553656a-5074-4bd9-bb80-3a0022588cf5" />

<br>
<br>

## 👨‍💻 프로젝트 소개

<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/da1b8b75-defd-4207-ab14-77e317757b79" />

<img width="1920" height="2738" alt="image" src="https://github.com/user-attachments/assets/2da1684e-52a4-48c5-91bb-4d45cc29b96d" />

<br>
<br>

## 🙌 팀원 구성

<div align="center">

| **전유연** | **노창준** | **조영찬** | **이예나** |
| :------: |  :------: | :------: | :------: |
| [<img src="https://avatars.githubusercontent.com/u/109857975?v=4" height=150 width=150> <br/> @youyeon11](https://github.com/youyeon11) | [<img src="https://avatars.githubusercontent.com/u/112895293?v=4" height=150 width=150> <br/> @geniusjun](https://github.com/geniusjun) | [<img src="https://avatars.githubusercontent.com/u/80813773?v=4" height=150 width=150> <br/> @yc3697](https://github.com/yc3697) | [<img src="https://avatars.githubusercontent.com/u/128028246?v=4" height=150 width=150> <br/> @lyemee](https://github.com/lyemee) |


</div>

<br>

## 🔧 기술 스택
- **Spring Boot 3.5.3**  
  - 애플리케이션 서버 (Java 17)
- **MySQL 8.0.33**  
  - RDBMS
- **Redis**  
  - 캐싱, Pub/Sub(실시간 데이터 전송), Refresh Token 관리
- **AWS S3**  
  - 레포지토리 업로드 저장소
- **Nginx**  
  - 서버 서빙, 리버스 프록시, 인증서 관리  
  - **Certbot** : 인증서 발급 자동화
- **배포 환경** : AWS EC2 (Ubuntu) + Nginx + Certbot


---

## 📖 브랜치 전략

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

## 📖 Pull Request 컨벤션

`[<Prefix>] <Description>`의 양식을 준수하되, prefix는 commit message convention을 따릅니다.

| **예시: [feat] 카카오 로그인 구현**

- PR 생성 시 24시간 이내에 확인을 요합니다.
- `develop` 브랜치로의 병합은 **최소 1명 이상의 리뷰어 승인(Approve)** 이 필요합니다.

---

## 📖 Commit Convention

`<Prefix>: <Description> (#<Issue_Number>)` 의 양식을 준수합니다.
- **feat**: 새로운 기능 구현`feat: 구글 로그인 API 기능 구현 (#11)`
- **fix**: 코드 오류 수정`fix: 회원가입 비즈니스 로직 오류 수정 (#10)`
- **del**: 불필요한 코드 삭제`del: 불필요한 import 제거 (#12)`
- **docs**: README나 wiki 등의 문서 개정`docs: 리드미 수정 (#14)`
- **refactor**: 내부 로직은 변경 하지 않고 기존의 코드를 개선하는 리팩터링`refactor: 코드 로직 개선 (#15)`
- **chore**: 의존성 추가, yml 추가와 수정, 패키지 구조 변경, 파일 이동 등의 작업 `chore: yml 수정 (#21)`, `chore: lombok 의존성 추가 (#22)`
- **test**: 테스트 코드 작성, 수정 `test: 로그인 API 테스트 코드 작성 (#20)`
---

## 📖 프로젝트 구조 (Domain Driven Design)

전통적인 **계층형 아키텍처(Layered Architecture)** 구조를 기반으로 패키지를 구성합니다.
```dockerignore
🔥hwaroak
 ┣ 📂.github
 ┃ ┣ 📂ISSUE_TEMPLATE        # 이슈 템플릿
 ┃ ┣ 📂workflows             # GitHub Actions 워크플로우 설정
 ┣ 📂nginx                   # Proxy/Nginx 설정
 ┃ ┣ 📂html
 ┣ 📂src
 ┃ ┗ 📂main
 ┃   ┣ 📂java
 ┃   ┃ ┗ 📂com
 ┃   ┃   ┗ 📂umc
 ┃   ┃     ┗ 📂hwaroak
 ┃   ┃       ┣ 📂service            # 도메인별 핵심 비즈니스 로직
 ┃   ┃       ┣ 📂controller         # API 엔드포인트(요청/응답 매핑)
 ┃   ┃       ┣ 📂config             # 프로젝트 전역 설정(Security, Web 등)
 ┃   ┃       ┣ 📂converter          # Entity ↔ DTO 변환 로직
 ┃   ┃       ┣ 📂domain             # 전역적으로 사용하는 도메인
 ┃   ┃       ┃ ┣ 📂entity           # JPA 엔티티(데이터베이스 매핑 클래스)
 ┃   ┃       ┣ 📂dto                # 클라이언트와 주고받는 데이터 객체
 ┃   ┃       ┃ ┣ 📂request          # 요청 DTO
 ┃   ┃       ┃ ┗ 📂response         # 응답 DTO
 ┃   ┃       ┣ 📂exception          # 전역 예외 정의 및 처리
 ┃   ┃       ┣ 📂repository         # JPA Repository 인터페이스
 ┃   ┃       ┣ 📂util               # 공통 유틸리티 클래스 모음
 ┃   ┃       ┣ 📂scheduler          # 배치/스케줄러: 알람·감정분석 등 만료/정리 작업 주기 실행
 ┃   ┃       ┣ 📂lock               # 분산/DB 락 유틸: 동시성 제어
 ┃   ┃       ┣ 📂infrastructure     # 인프라 어댑터: Redis, JWT 등 인증/캐시/외부자원 접근 레이어
 ┃   ┃       ┗ 📂event              # 이벤트 발행/리스너 패턴
 ┃   ┃       ┗ 📜HwaroakApplication.java   # Spring Boot 메인 실행 클래스
 ┃   ┗ 📂resources
 ┃     ┣ 📂static
 ┃     ┣ 📜application-dev.yaml     # 개발 프로필
 ┃     ┣ 📜application-local.yaml   # 로컬 프로필
 ┃     ┗ 📜application.yaml         # 공통 프로필
 ┣ 📜.env                            # 중요 환경변수
 ┣ 📜.gitignore
 ┣ 📜README.md
 ┗ 📜build.gradle

```

> 기능 단위로가 아닌 역할에 따른 계층 분리를 통해 각 레이어의 책임을 명확히 합니다.
>
---



## ⭐ 서버 아키텍처 다이어그램


<img width="1920" height="1115" alt="image" src="https://github.com/user-attachments/assets/957fb655-cc24-4fb3-8928-61541c2aa425" />



<br>

## ERD

<img width="1041" height="1175" alt="image" src="https://github.com/user-attachments/assets/4bd4c3a5-a13a-4985-926d-d8f38cb519ae" />

