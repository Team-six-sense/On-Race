# OnRace - Frontend

---

## 🛠 기술 스택

### Frontend

| 기술             | 버전   | 용도             |
| ---------------- | ------ | ---------------- |
| **Next.js**      | 16.0.7 | React 프레임워크 |
| **React**        | 19.2.0 | UI 라이브러리    |
| **TypeScript**   | 5.x    | 타입 안정성      |
| **Tailwind CSS** | 4.x    | 스타일링         |

---

## 📁 프로젝트 구조

```
front/
├── src/
│   ├── app/                  # Next.js App Router (페이지 및 라우팅)
│   │   ├── api/              # 서버리스 API Route (Backend Proxy 등)
│   │   │   ├── template/     # API 개발용 참조 템플릿
│   │   │   ├── auth/         # 인증 관련 API 엔드포인트
│   │   │   └── mock/         # 개발 및 테스트용 Mock API
│   │   ├── (auth)/           # 인증 관련 페이지 그룹
│   │   │   ├── login/        # 로그인 페이지
│   │   │   └── signup/       # 회원가입 페이지
│   │   ├── (main)/           # 메인 서비스 페이지 그룹
│   │   │   ├── design/       # 공통 디자인 시스템 가이드 페이지
│   │   │   ├── schedule/     # 전체 이벤트 일정 페이지
│   │   │   └── ticketing/    # 티켓 예매 및 관리 페이지
│   ├── components/           # 재사용 가능한 UI 컴포넌트 (Presentational)
│   │   ├── layout/           # 전역 레이아웃 요소 (Header, Footer, Sidebar 등)
│   │   ├── shadcn/           # shadcn/ui 기반 외부 라이브러리 컴포넌트
│   │   └── ui/               # 디자인 시스템 UI 컴포넌트 (Button, Input 등)
│   ├── features/             # 도메인별 비즈니스 로직 및 복합 컴포넌트 (Container)
│   │   ├── template/         # 신규 기능 구현을 위한 참조용 보일러플레이트(템플릿)
│   │   │   ├── components/   # 해당 기능 전용 UI 컴포넌트 (해당 도메인 내 재사용)
│   │   │   ├── hooks/        # 해당 기능의 상태 관리 및 로직을 분리한 전용 Custom Hooks
│   │   │   ├── services/     # 해당 기능과 관련된 API 호출 함수 및 데이터 처리 로직
│   │   │   ├── types/        # 해당 기능에서 사용되는 고유 데이터 구조 및 타입 정의
│   │   │   └── utils/        # 해당 기능 내에서만 사용되는 전용 유틸리티 및 헬퍼 함수
│   │   ├── auth/             # 인증 및 사용자 계정 관련 비즈니스 로직
│   │   ├── design/           # 디자인 시스템 샘플 및 테스트 컴포넌트
│   │   ├── schedule/         # 이벤트 일정 관련 기능
│   │   └── ticketing/        # 예매 프로세스 기능
│   ├── hooks/                # 프로젝트 전역 공통 Custom Hooks
│   ├── lib/                  # 외부 라이브러리 설정 및 인스턴스 (Axios, Fetcher 등)
│   ├── mockups/              # 프론트엔드 테스트용 정적 데이터 (JSON 등)
│   ├── providers/            # 전역 Context Provider (Query, Theme, Auth 등)
│   ├── store/                # 상태 관리 스토어 (Zustand, Recoil 등)
│   ├── style/                # 전역 스타일 설정 (Global CSS, Tailwind Config 등)
│   ├── types/                # TypeScript 공통 타입 및 인터페이스 정의
└── └── utils/                # 공통 유틸리티 함수 (Format, Validator 등)
```

---

## 🏁 시작하기

### 사전 요구 사항

- Node.js 18.x 이상
- npm 또는 yarn

### 설치

```bash
# 저장소 클론
git clone https://github.com/Team-six-sense/On-Race.git
cd frontend

# 의존성 설치
npm install
```

### 개발 서버 실행

```bash
npm run dev      # Next.js 개발 서버 (포트 3000)
```

### 접속

브라우저에서 [http://localhost:3000](http://localhost:3000) 열기

---

## 🔧 환경 변수 설정

프로젝트 루트에 `.env` 파일 생성:

```env
NEXT_PUBLIC_API_MODE=mock

# NextAuth 기본 설정
NEXTAUTH_URL=http://localhost:3000
NEXTAUTH_SECRET=your_nextauth_secret_key_should_be_a_long_random_string

# Google (https://console.cloud.google.com/)
GOOGLE_CLIENT_ID=000000000000-example-hash-string.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-example-secret-key-123456789

# Kakao (https://developers.kakao.com/)
KAKAO_CLIENT_ID=your_kakao_rest_api_key_here
KAKAO_CLIENT_SECRET=your_kakao_client_secret_here

# Naver (https://developers.naver.com/)
NAVER_CLIENT_ID=your_naver_client_id_here
NAVER_CLIENT_SECRET=your_naver_client_secret_here
```

---

## 📜 스크립트

| 명령어          | 설명                   |
| --------------- | ---------------------- |
| `npm run dev`   | Next.js 개발 서버 실행 |
| `npm run build` | 프로덕션 빌드          |
| `npm start`     | 프로덕션 서버 시작     |

---
