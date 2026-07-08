# 임시 구조 점검 보고서

대상 저장소는 Spring Boot 기반의 게시판형 커뮤니티 서비스다. 현재 구조만 기준으로 보면 인증, 보드, 게시글, 프로필, 관리자, 알림, 업로드까지 서비스 핵심 기능은 대부분 구현되어 있다.

## 1. 전체 구조

- 진입점은 `src/main/java/org/java/spring_04/Spring04Application.java` 이다.
- 화면 라우팅은 `src/main/java/org/java/spring_04/common/PageController.java` 와 `src/main/java/org/java/spring_04/BoardRouter.kt` 가 담당한다.
- REST API는 `auth`, `board`, `post`, `feature`, `profile`, `common` 계열로 나뉜다.
- 데이터 초기화 및 스키마 보정 로직이 여러 서비스의 `@PostConstruct` 에 분산되어 있다.

## 2. 핵심 기능 범위

### 인증

- 로그인
- 관리자 로그인
- 이메일 인증 기반 회원가입
- 아이디, 닉네임, 이메일, 비밀번호 정책 검증
- 로그인 상태 확인
- 로그아웃

관련 파일:

- `src/main/java/org/java/spring_04/auth/AuthController.java`
- `src/main/java/org/java/spring_04/auth/AuthService.java`

### 보드

- 보드 목록
- 토픽 목록
- 내 보드 대시보드
- 게시글 목록과 상세
- 보드 관리 화면
- 보드 설정 조회 및 수정
- 카테고리, 태그 변경
- 보드 매니저, 서브매니저 관리
- 보드 랭킹
- 가입 요청 및 사이드보드 요청
- 글쓰기

관련 파일:

- `src/main/java/org/java/spring_04/board/BoardController.java`
- `src/main/java/org/java/spring_04/board/BoardService.java`

### 게시글

- 게시글 목록
- 추천 게시글
- 게시글 상세
- 글 작성
- 댓글 작성
- 추천, 비추천
- 글 삭제
- 댓글 삭제

관련 파일:

- `src/main/java/org/java/spring_04/post/PostController.java`
- `src/main/java/org/java/spring_04/post/PostService.java`

### 부가 기능

- 검색
- 보드 가입 신청 및 승인
- 스크랩
- 신고
- 콘셉트 설정 및 해제
- 글 끌올
- 공지 지정
- 카테고리 지정
- 댓글 좋아요, 댓글 신고
- 사용자 차단, 차단 해제
- 알림 설정, 전체 읽음 처리
- 금칙어 관리
- 보드 밴 관리
- 회원 탈퇴

관련 파일:

- `src/main/java/org/java/spring_04/feature/FeatureController.java`
- `src/main/java/org/java/spring_04/feature/FeatureService.java`

### 프로필

- 내 프로필 조회
- 공개 프로필 조회
- 프로필 설정 저장
- 히스토리 삭제
- 팔로우, 언팔로우

관련 파일:

- `src/main/java/org/java/spring_04/profile/ProfileController.java`
- `src/main/java/org/java/spring_04/profile/ProfileService.java`

### 관리자

- 관리자 전용 보드 관리 페이지
- 요청 관리 페이지
- 사용자 검색
- 관리자 요청 처리
- 보드 매니저 및 서브매니저 조작

관련 파일:

- `src/main/java/org/java/spring_04/common/AdminController.java`

### 파일 업로드

- GCS 이미지 업로드
- 보드별 이미지 업로드 정책 검증

관련 파일:

- `src/main/java/org/java/spring_04/common/UploadController.java`

## 3. 데이터 및 런타임 구조

- 런타임 기준 DB는 MySQL이다.
- `src/main/resources/application.properties` 에는 SQLite는 사용하지 않는다고 적혀 있다.
- 루트의 `launch.py`, `viewer.py`, `post.py`, `pair.py`, `inserter.py` 는 SQLite 기반 유틸 또는 초기화 스크립트로 보인다.
- 실제 서비스는 `StartupInput` 이 MySQL JDBC URL, 서버 포트, 메일 계정을 런타임에 주입하는 구조다.

관련 파일:

- `src/main/resources/application.properties`
- `src/main/java/org/java/spring_04/common/StartupInput.java`
- `launch.py`

## 4. 운영 관찰 사항

- 관리자 로그인 코드와 허용 IP가 설정 파일에 직접 들어 있다.
- 세션 쿠키 `secure=false` 로 되어 있어 HTTPS 운영 시 재검토가 필요하다.
- GCS 버킷, SMTP, DB 설정이 모두 외부 의존성을 전제로 한다.
- 스키마 생성과 마이그레이션이 시작 시 자동 수행되므로 배포 시 DB 상태에 민감하다.

관련 파일:

- `src/main/resources/application.properties`
- `src/main/java/org/java/spring_04/common/DatabaseConfig.java`
- `src/main/java/org/java/spring_04/common/StartupInput.java`
- 각 서비스의 `@PostConstruct` 초기화 구간

## 5. 점검 중 확인된 주의점

- `BoardController` 에는 현재 코드상 문법 오류로 보이는 라인이 있다.
- Maven 빌드는 로컬 환경 제약 때문에 완전히 끝까지 검증하지 못했다.
- Kotlin daemon 임시 파일 접근 권한 문제로 컴파일이 중단되었다.

관련 파일:

- `src/main/java/org/java/spring_04/board/BoardController.java`

## 6. 결론

기능 범위는 실서비스 수준에 가깝고, 주요 사용자 흐름은 대부분 갖춰져 있다.
다만 운영 측면에서는 다음이 중요하다.

- DB 마이그레이션 체계 분리
- 민감 설정의 환경변수화
- HTTPS 세션 보안 재점검
- 빌드 및 배포 환경에서의 실제 컴파일 검증
