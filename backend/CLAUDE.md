# EcoSync Backend

Spring Boot 4.0.3 / Java 25 / Gradle 9.3.1 멀티모듈 백엔드.
헥사고날 아키텍처(Ports & Adapters) 기반으로 API 서버와 Batch 서버를 독립 실행.

**코딩 컨벤션** → `docs/backend-conventions.md` 참고

---

## 기술 스택

| 항목 | 내용 |
|---|---|
| 언어 / 빌드 | Java 25, Gradle 9.3.1 |
| 프레임워크 | Spring Boot 4.0.3 |
| ORM | Spring Data JPA, Hibernate |
| 매핑 | MapStruct 1.5.5.Final |
| 캘린더 | ical4j 3.2.19 |
| DB | MySQL 8 (로컬: Docker Compose) |

---

## 모듈 구조

```
eco-sync-domain/         # 순수 도메인 — Spring/JPA 의존 없음
eco-sync-application/    # UseCase(Input Port) + Output Port 인터페이스
eco-sync-infrastructure/ # Output Adapter (JPA, 외부 API, ICS 생성)
eco-sync-api/            # REST Controller + API 서버 Bootstrap (포트 8080)
eco-sync-batch/          # Spring Batch Job + Batch 서버 Bootstrap (포트 8081)
```

**의존성 방향** (항상 바깥 → 안쪽)

```
eco-sync-api   ──► eco-sync-application ──► eco-sync-domain
eco-sync-batch ──►         ▲
                           │
          eco-sync-infrastructure ──► eco-sync-application
                                  ──► eco-sync-domain
```

| 모듈 | Spring-web | JPA | 비고 |
|---|---|---|---|
| domain | ✗ | ✗ | 순수 Java |
| application | Context만 | ✗ | Port 인터페이스 |
| infrastructure | ✗ | ✓ | Adapter 구현 |
| api | ✓ | ✓ | bootJar |
| batch | ✓ | ✓ | bootJar |

---

## 실행

```bash
# API 서버 (포트 8080)
./gradlew :eco-sync-api:bootRun

# Batch 서버 (포트 8081)
./gradlew :eco-sync-batch:bootRun

# 전체 빌드 (테스트 제외)
./gradlew build -x test
```

---

## API 엔드포인트

```
GET  /api/countries                      # 지원 국가 목록
POST /api/subscriptions                  # 구독 등록
GET  /api/calendar/{token}/subscribe     # ICS 파일 스트리밍 (캘린더 연동)
```

---

## Batch 스케줄

| Job | Cron | 설명 |
|---|---|---|
| HolidayCollectionJob | `0 0 2 * * SUN` | 매주 일요일 02:00, 다음 1년치 수집 |
| IndicatorCollectionJob | `0 0 3 * * SUN` | 매주 일요일 03:00 |
| EarningsCollectionJob | `0 0 4 * * SUN` | 매주 일요일 04:00 |

---

## 외부 API 연동

| 용도 | API | 비고 |
|---|---|---|
| 국가별 휴장일 | Nager.Date API | 무료 |
| 경제지표 | FRED API | 무료, 1,000회/일 |
| 기업 실적 | Yahoo Finance | Phase 3 |

---

## DB 스키마

전체 DDL → `infra/local/sql/01_schema.sql`

| 테이블 | 설명 |
|---|---|
| `subscriptions` | 구독자. `calendar_token`(UUID)으로 ICS URL 생성. 재구독 시 `deleted_at` 복원. |
| `subscription_interests` | 구독 관심사. `interest_type` + `interest_value`로 Phase 1~3 커버. |
| `economic_events` | 경제 이벤트 마스터. `uid`(iCal UID) 기준 배치 upsert. |

**`subscription_interests` interest_type 값**

| Phase | interest_type | interest_value 예시 |
|---|---|---|
| 1 | `COUNTRY` | `KR`, `US`, `JP` |
| 2 | `IMPORTANCE` | `HIGH`, `MEDIUM`, `LOW` |
| 3 | `TICKER` | `AAPL`, `005930` |

---

## 설정 파일 구조

```
eco-sync-api/src/main/resources/
├── application.yaml          # 프로파일 정의 (기본: local)
└── local/
    ├── server.yaml           # port: 8080
    ├── database.yaml         # MySQL 연결
    └── logging.yaml          # 로그 레벨
```

환경별 실행: `--args='--spring.profiles.active=dev'`

---

## Git 컨벤션

### 브랜치

```
main ← dev ← feature/TSK-*
                          bugfix/TSK-*
                          chore/TSK-*
                          docs/TSK-*
```

### 커밋 메시지

```
[TYPE-TSK-번호] 제목

TYPE: feat | fix | refactor | docs | infra | chore | test
```

### 커밋 그룹핑 원칙

- SQL/인프라 변경은 함께 커밋
- 레이어별로 분리 커밋 (domain / application / infrastructure / config)
- 한 커밋에 한 가지 관심사
