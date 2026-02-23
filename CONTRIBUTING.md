# Contributing Guide

On-Race Monorepo 협업 규칙 문서입니다.  
브랜치, 커밋, PR, 리뷰/머지 컨벤션을 아래 기준으로 통일합니다.

---

## 1) 브랜치 전략 (Git-Flow 변형 / Monorepo)

### 주요 브랜치

- `main`
  - 정의: **[배포]** 실제 운영 환경(Production)에 배포되는 코드
  - 규칙: 직접 커밋 금지
  - 비고: Merge 시 Production EKS 배포 파이프라인 자동 실행

- `develop`
  - 정의: **[개발/스테이징]** 기능 통합 및 테스트 브랜치
  - 규칙: 기능 완료 후 `develop` 대상으로 PR
  - 비고: Merge 시 Staging(Dev) EKS 배포 파이프라인 자동 실행

### 보조 브랜치

- `feat/<서비스명>/<기능명>`
  - 목적: 새로운 기능 개발
  - 규칙: Monorepo 구조상 서비스 폴더명을 포함 권장 (경로 필터링 용이)
  - 예시: `feat/backend/login-auth`, `feat/frontend/landing-page`, `feat/infra/k8s-setting`

- `fix/<기능명>`
  - 목적: `develop` 단계 버그 수정

- `hotfix/<기능명>`
  - 목적: `main`(운영) 단계 긴급 버그 수정

- `docs/<문서명>`
  - 목적: 문서 수정

### 기본 워크플로우

1. `develop` 최신 코드 pull
2. 작업 브랜치 생성 (`feat/...`, `fix/...` 등)
3. 기능 개발 및 로컬 검증
4. PR 생성 및 동료 리뷰/승인
5. 규칙 충족 후 머지

---

## 2) 커밋 메시지 컨벤션

### 작성 규칙

- 타입과 scope는 영문 소문자
- 제목은 한글로 작성
- 콜론(`:`) 뒤 한 칸 띄우기
- 제목은 명사형 종결 권장 (`~구현`, `~수정`, `~적용`)
- 제목 끝 마침표 금지
- 이슈 번호는 꼬리말로 `Resolves: #123` 형식 명시

### 기본 형식

```text
타입(scope): 한글 제목

- 상세 변경 내용 1
- 상세 변경 내용 2
Resolves: #이슈번호
```

### 예시

```text
feat(backend): 좌석 임시 점유 Redis 락 구현
- 동시 예약 요청 시 분산락 적용
- 점유 만료 시간 정책 반영
Resolves: #2
```

### 타입 키워드

- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `infra`: 인프라 구성 변경 (Terraform, K8s, Helm)
- `chaos`: 카오스 엔지니어링 테스트 (장애 주입)
- `security`: 보안 설정 (WAF, SG, IAM)
- `docs`: 문서 변경
- `style`: 코드 포맷/스타일 변경 (로직 변경 없음)
- `refactor`: 리팩토링
- `test`: 테스트 코드 추가/수정
- `chore`: 빌드/설정/기타 유지보수

### Scope 가이드 (Monorepo 폴더명 일치 필수)

- `frontend`: `/frontend` 변경
- `backend`: `/backend` 변경
- `ai`: `/ai` 변경
- `infra`: `/infra` 변경
- `docs`: 문서 작업 (`README.md` 등)

---

## 3) PR 컨벤션

### PR 제목 형식

`[타입(scope)] 제목`

- 예시: `[feat(backend)] 좌석 예약 대기열 Redis 로직 구현`
- 예시: `[infra(k8s)] Ingress 라우팅 규칙 수정`

### PR 템플릿

```md
## 🎯 작업 타겟 (Monorepo)
- [ ] Backend
- [ ] Frontend
- [ ] AI
- [ ] Infra / DevOps

## 🔍 작업 내용 (What)
- 변경된 기능이나 수정 사항을 간단히 나열해주세요.

## 📸 스크린샷 / 아키텍처 / 로그 (필수)
- UI 변경 시 스크린샷 첨부 (Frontend 필수)
- 로직 변경 시 관련 로그/다이어그램/테스트 결과 캡처 첨부 (Backend/Infra 필수)

## ❓ 변경 이유 (Why)
- 왜 이 작업이 필요했는지 설명해주세요.

## 📋 체크리스트
- [ ] 로컬 환경에서 빌드 및 테스트 성공 확인
- [ ] 민감 정보(AWS Key, DB 비번) 하드코딩 없음 (OIDC/Secret 사용 확인)
- [ ] 불필요한 로그(`console.log`, `print`) 제거
- [ ] 다른 서비스(예: 백엔드 수정 시 프론트)에 영향 없는지 확인

## 🔗 관련 이슈
Resolves: #
```

### 라벨 규칙

PR 작성 시 아래 라벨을 선택합니다.

- 영역 라벨(1개 이상): `Frontend`, `Backend`, `AI`, `Infra`, `Security`, `Docs`
- 성격 라벨(1개 이상): `bug`, `enhancement`, `documentation`, `critical`, `feat`

---

## 4) 코드리뷰 및 머지 컨벤션

### 머지 전략

- 기본 원칙: **Squash and Merge**
- 이유: Monorepo에서 히스토리를 기능 단위로 깔끔하게 유지
- 주의: Squash 시 최종 커밋 메시지도 컨벤션(`type(scope): 제목`) 준수

### 리뷰/승인 규칙

- Approve 최소 1명 이상 필수
- Self-Merge 금지 (본인 PR 병합 금지)
- 운영 장애 대응 긴급 Hotfix는 예외 가능
- 리뷰 매너: 리뷰어는 건설적 피드백, 작성자는 열린 수용 태도 유지

### 리뷰 체크리스트

| 구분 | 체크 항목 | 비고 |
| --- | --- | --- |
| 기본 사항 | [ ] 변수명, 주석, UI 텍스트 오타 확인 | 가독성/전문성 |
|  | [ ] 불필요한 로그(`console.log`, `print`) 제거 | 운영 환경 오염 방지 |
|  | [ ] 주석 처리된 코드(Dead Code) 삭제 | 기술 부채 방지 |
|  | [ ] 브랜치(영문), 커밋(한글) 컨벤션 준수 | 히스토리 관리 |
|  | [ ] `.DS_Store`, `node_modules` 등 불필요 파일 제외 | `.gitignore` 확인 |
| 로직/보안 | [ ] 민감 정보(API Key, DB 비번) 하드코딩 여부 점검 | 보안 최우선 |
|  | [ ] 로컬 환경에서 정상 동작 확인 | 기능 무결성 |
|  | [ ] 변수/함수명이 역할을 설명하는가 | 유지보수성 |
|  | [ ] 단일 책임 원칙을 지키는가 | 코드 구조 |
|  | [ ] 기존 기능 영향(사이드 이펙트) 설명이 있는가 | 회귀 방지 |

### 충돌 해결 원칙

- 충돌은 PR 작성자가 로컬에서 해결 후 push
- GitHub 웹 에디터로 충돌 해결 금지 (히스토리 꼬임 방지)

---

## 5) 보안/운영 기본 원칙

- 시크릿, 토큰, 비밀번호는 코드에 하드코딩 금지
- 환경 변수/시크릿 매니저 사용
- 민감 정보 노출 시 즉시 팀에 공유하고 키 폐기/재발급 진행
