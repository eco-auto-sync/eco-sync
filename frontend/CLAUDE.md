# EcoSync Frontend

Next.js 16 / React 19 / TypeScript / Tailwind CSS 4 기반 프론트엔드.

> **주의**: Next.js 16은 이전 버전과 Breaking Change가 있음.
> 코드 작성 전 `node_modules/next/dist/docs/` 내 관련 가이드를 먼저 확인할 것.

---

## 기술 스택

| 항목 | 버전 |
|---|---|
| Next.js | 16.2.4 (App Router) |
| React | 19.2.4 |
| TypeScript | 5.x |
| Tailwind CSS | 4.x |

---

## 실행

```bash
cd frontend
cp .env.example .env.local   # NEXT_PUBLIC_API_URL 확인
npm install
npm run dev    # http://localhost:3000
```

### 환경 변수

| 변수 | 기본값 | 설명 |
|---|---|---|
| `NEXT_PUBLIC_API_URL` | `http://localhost:8080` | Spring Boot API 서버 주소 |

---

## 디렉토리 구조

```
frontend/src/
├── app/                     # Next.js App Router
│   ├── layout.tsx           # 전체 레이아웃 (Header 포함)
│   ├── page.tsx             # 홈 (/)
│   ├── subscribe/
│   │   ├── page.tsx         # 국가 선택 + 구독 신청 (/subscribe)
│   │   └── complete/
│   │       └── page.tsx     # 구독 완료 (/subscribe/complete)
│   └── my/
│       └── page.tsx         # 내 구독 관리 (/my)
├── components/
│   ├── layout/              # Header 등 전역 레이아웃 컴포넌트
│   └── ui/                  # 재사용 UI 컴포넌트 (CountryCard 등)
├── hooks/                   # 커스텀 React Hook
├── lib/
│   ├── api.ts               # fetch 기반 API 클라이언트
│   └── mock-data.ts         # 개발용 목 데이터 (API 연동 전 임시)
└── types/
    └── index.ts             # 공통 타입 (ApiResponse, PageResponse)
```

---

## API 클라이언트

`src/lib/api.ts`에 fetch 래퍼가 정의되어 있음. `Content-Type: application/json` 기본 설정.

```ts
import { api } from "@/lib/api";

// GET
const countries = await api.get<Country[]>("/api/countries");

// POST
const subscription = await api.post<Subscription>("/api/subscriptions", {
  email: "user@example.com",
  countryCodes: ["KR", "US"],
});

// PUT / DELETE
await api.put<Subscription>("/api/subscriptions/1", { ... });
await api.delete("/api/subscriptions/1");
```

응답 형식 (`src/types/index.ts`):

```ts
interface ApiResponse<T> { data: T; message?: string; }
interface PageResponse<T> { content: T[]; totalElements: number; totalPages: number; size: number; number: number; }
```

---

## 컴포넌트 컨벤션

- 서버 컴포넌트가 기본. 상태·이벤트 핸들러가 필요한 경우에만 `'use client'`
- 파일명: PascalCase (`CountryCard.tsx`)
- 페이지 컴포넌트: `export default function XxxPage()`
- 레이아웃 컴포넌트: `src/components/layout/`
- 재사용 UI: `src/components/ui/`

---

## 스타일 컨벤션

Tailwind CSS 유틸리티 클래스만 사용. 별도 CSS 파일 작성 금지 (`globals.css` 제외).

**컬러 팔레트** (기존 화면과 통일):

| 용도 | 클래스 |
|---|---|
| 주요 액션 (버튼, 링크) | `bg-zinc-900`, `hover:bg-zinc-700` |
| 강조 (Phase 1, 선택 상태) | `emerald-*` |
| 텍스트 계층 | `text-zinc-900` / `text-zinc-500` / `text-zinc-400` |
| 테두리 / 배경 | `border-zinc-200`, `bg-white`, `bg-zinc-50` |
| 버튼 모양 | `rounded-full` (주요 버튼), `rounded-2xl` (카드) |

**반응형**: `sm:`, `md:` 브레이크포인트 활용. 모바일 우선 작성.
