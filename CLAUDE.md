# EcoSync

사용자가 관심 있는 경제 일정(국가별 증시 휴장일, 경제지표 발표일, 기업 실적 발표일)을 선택하면
Google Calendar 등 외부 캘린더 앱에서 ICS 구독으로 자동 확인할 수 있는 서비스.

## 문서 관리

프로젝트 관련 문서는 `docs/` 폴더에서 관리한다.

```
docs/
├── phase1-spec.md   # Phase 1 기능 명세서 (화면·API·배치·데이터)
├── fmp-api.md       # Financial Modeling Prep API 정리
└── ...
```

---

## 모노레포 구조

```
eco-sync/
├── backend/    # Spring Boot — API 서버 + Batch 서버 (헥사고날 멀티모듈)
├── frontend/   # Next.js 16 — React + TypeScript + Tailwind CSS
├── infra/      # 공통 인프라 (Docker Compose — MySQL, phpMyAdmin)
└── docs/       # 프로젝트 문서
```

---

## Backend

### 실행

```bash
# 로컬 인프라 (MySQL 3306, phpMyAdmin 8888)
cd infra/local
cp .env.example .env   # MYSQL_ROOT_PASSWORD, MYSQL_USER, MYSQL_PASSWORD, MYSQL_DATABASE 설정
docker-compose up -d

# API 서버 (포트 8080)
./gradlew :eco-sync-api:bootRun

# Batch 서버 (포트 8081)
./gradlew :eco-sync-batch:bootRun
```

### 모듈 구조 (헥사고날 아키텍처)

```
eco-sync-domain/         # 순수 도메인 — Spring/JPA 의존 없음
eco-sync-application/    # UseCase(Input Port) + Output Port 인터페이스 정의
eco-sync-infrastructure/ # Output Adapter 구현체 (JPA, 외부 API, ICS 생성)
eco-sync-api/            # Input Adapter (REST Controller) + API 서버 Bootstrap
eco-sync-batch/          # Input Adapter (Spring Batch Job) + Batch 서버 Bootstrap
```

**의존성 방향 (항상 바깥 → 안쪽)**
```
eco-sync-api      ──► eco-sync-application ──► eco-sync-domain
eco-sync-batch    ──►         ▲
                              │
               eco-sync-infrastructure ──► eco-sync-application
                                      ──► eco-sync-domain
```

**의존성 규칙**
- `domain`: 외부 의존 없음 (순수 Java)
- `application`: domain만 의존, Spring-web / JPA 참조 금지
- `infrastructure`: application + domain 의존
- `api` / `batch`: 서로 참조 금지

### 주요 도메인 모델 (`eco-sync-domain`)

| 패키지 | 클래스 | 설명 |
|---|---|---|
| `subscription` | `Subscription` | 구독 Aggregate Root |
| `subscription` | `SubscriptionToken` | ICS URL용 UUID VO |
| `country` | `Country`, `Holiday` | 국가 + 증시 휴장일 |
| `indicator` | `EconomicIndicator`, `IndicatorSchedule` | 경제지표 + 발표 일정 |
| `stock` | `Stock`, `EarningsDate` | 종목 + 실적 발표일 |

### API 엔드포인트

```
GET  /api/countries                         # 지원 국가 목록
POST /api/subscriptions                     # 구독 등록
POST /api/subscriptions/{id}/indicators     # 구독에 경제지표 추가
GET  /api/calendar/{token}/subscribe        # ICS 파일 스트리밍 (캘린더 연동)
GET  /api/subscriptions/{id}/sync           # 수동 동기화
```

### Batch 스케줄

| Job | Cron | 설명 |
|---|---|---|
| HolidayCollectionJob | `0 0 2 * * SUN` | 매주 일요일 02:00, 다음 1년치 수집 |
| IndicatorCollectionJob | `0 0 3 * * SUN` | 매주 일요일 03:00 |
| EarningsCollectionJob | `0 0 4 * * SUN` | 매주 일요일 04:00 |

### 외부 API 연동

| 용도 | API | 비고 |
|---|---|---|
| 국가별 휴장일 | Nager.Date API | 무료, 대체 API 확보 필요 |
| 경제지표 | FRED API | 무료 1,000회/일 제한 |
| 기업 실적 | Yahoo Finance | Phase 3 |

### DB 스키마

전체 DDL → `docs/schema.sql`

| 테이블 | 설명 |
|---|---|
| `subscriptions` | 구독자. `calendar_token`(UUID)으로 ICS URL 생성. 재구독 시 `deleted_at` 복원. |
| `subscription_interests` | 구독 관심사. `interest_type` + `interest_value`로 Phase 1~3 커버. |
| `economic_events` | 경제 이벤트 마스터. `uid`(iCal UID) 기준 배치 upsert. `event_date`는 UTC. |

**`subscription_interests` interest_type 값**

| Phase | interest_type | interest_value 예시 |
|---|---|---|
| 1 | `COUNTRY` | `KR`, `US`, `JP` |
| 2 | `IMPORTANCE` | `HIGH`, `MEDIUM`, `LOW` |
| 3 | `TICKER` | `AAPL`, `005930` |

모든 테이블 소프트 딜리트 (`deleted_at IS NULL` = 활성).
`subscription_interests` ↔ `economic_events` 간 DB FK 없음 — ICS 생성 시 애플리케이션 레벨에서 `category` + `interest_value` 기준으로 필터링.

---

## Frontend

### 실행

```bash
cd frontend
cp .env.example .env.local   # NEXT_PUBLIC_API_URL 확인
npm install
npm run dev    # http://localhost:3000
```

### 디렉토리 구조

```
frontend/src/
├── app/                # Next.js App Router (페이지 = 폴더)
│   ├── layout.tsx      # 전체 레이아웃, 메타데이터
│   └── page.tsx        # 루트 페이지 (/)
├── components/
│   ├── layout/         # Header 등 공통 레이아웃 컴포넌트
│   └── ui/             # 재사용 UI 컴포넌트
├── hooks/              # 커스텀 React Hook
├── lib/
│   └── api.ts          # fetch 기반 API 클라이언트 (api.get / post / put / delete)
└── types/
    └── index.ts        # 공통 타입 (ApiResponse, PageResponse)
```

### API 클라이언트 사용 예시

```ts
import { api } from "@/lib/api";

// GET
const countries = await api.get<Country[]>("/api/countries");

// POST
const subscription = await api.post<Subscription>("/api/subscriptions", {
  email: "user@example.com",
  countryCodes: ["KR", "US"],
});
```

### 환경 변수

| 변수 | 기본값 | 설명 |
|---|---|---|
| `NEXT_PUBLIC_API_URL` | `http://localhost:8080` | Spring Boot API 서버 주소 |

---

## 개발 로드맵

| Phase | 기간 | 목표 |
|---|---|---|
| **Phase 1** (Now) | 2026.03.10 ~ 04.05 | 국가별 증시 휴장일 ICS 구독 MVP |
| **Phase 2** (Next) | 2026.04.06 ~ 05.17 | 금리·CPI·GDP 등 경제지표 캘린더 연동 |
| **Phase 3** (Later) | 2026.05.18 ~ 06.28 | 기업 실적 발표일 구독 + D-3 사전 알림 |

Phase 2, 3은 Phase 1의 ICS 인프라(토큰 기반 구독 URL) 위에 빌드됨.

---
