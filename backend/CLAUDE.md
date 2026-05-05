# EcoSync Backend

Spring Boot 4.0.3 / Java 25 / Gradle 9.3.1 멀티모듈 백엔드.
헥사고날 아키텍처(Ports & Adapters) 기반으로 API 서버와 Batch 서버를 독립 실행.

---

## 기술 스택

| 항목 | 내용 |
|---|---|
| 언어 / 빌드 | Java 25, Gradle 9.3.1 |
| 프레임워크 | Spring Boot 4.0.3 |
| ORM | Spring Data JPA, Hibernate |
| 동적 쿼리 | QueryDSL (OpenFeign fork 7.1) |
| 매핑 | MapStruct 1.5.5.Final |
| 캘린더 | ical4j 3.2.19 |
| DB | MySQL 8 (로컬: Docker Compose) |
| API 문서 | springdoc-openapi 3.0.3 (`/swagger-ui/index.html`) |

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

| 모듈 | 주요 의존성 | 비고 |
|---|---|---|
| domain | 없음 | 순수 Java |
| application | spring-context, spring-web, spring-tx | Port 인터페이스 + UseCase 서비스 |
| infrastructure | spring-data-jpa, querydsl-jpa, mapstruct | Adapter 구현 |
| api | spring-web, spring-security, springdoc | bootJar |
| batch | spring-batch | bootJar |

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

## 개발 컨벤션

### Gradle 의존성 규칙

- 모듈 간 의존성에 **`api` 설정 사용 금지** — 모두 `implementation`
- api/batch 모듈에서 JPA 애노테이션 사용 시 `spring-boot-starter-data-jpa`를 해당 모듈에 직접 선언

### Domain 레이어

기반 클래스 상속 계층:

```
BaseCreated   (createdAt)
  └─ BaseUpdated   (+ updatedAt)
       └─ BaseSoftDelete   (+ deletedAt, softDelete(), restore(), isActive())
```

- `@SuperBuilder` + `@NoArgsConstructor` + `@Getter` 세트 사용
- `@SuperBuilder` 이유: MapStruct `toDomain()` 시 상속 필드까지 빌더에 포함해야 함
- 소프트 딜리트 메서드명: `softDelete()` / `restore()`
- **`@SuperBuilder` + Stream 주의**: `.map(x -> Builder.builder()...build()).toList()` 시 타입 추론 실패 → 람다 내 명시적 타입 변수 선언 필요

```java
// ❌ 컴파일 에러
.map(code -> SubscriptionInterest.builder()...build()).toList()

// ✅
.map(code -> {
    SubscriptionInterest interest = SubscriptionInterest.builder()...build();
    return interest;
}).toList()
```

```java
@Getter @SuperBuilder @NoArgsConstructor
public class Subscription extends BaseSoftDelete {
    private Long id;
    private String email;
    private String calendarToken;

    public static Subscription create(String email) { ... }  // 팩토리 메서드
}
```

### Application 레이어

- Input Port 위치: `com.ecosync.application.port.in` — UseCase 인터페이스
- Output Port 위치: `com.ecosync.application.port.out` — 인프라 구현체 계약
- 서비스 위치: `com.ecosync.application.service` — UseCase 구현
- 도메인 객체만 파라미터/반환 타입으로 사용 (JPA 엔티티 노출 금지)
- Spring 의존: `@Service`, `@Transactional` 허용 / JPA, WebClient 금지

**UseCase 인터페이스 패턴** — Command/Result를 inner record로 정의:

```java
public interface CreateSubscriptionUseCase {
    record Command(String email, List<String> countryCodes) {}
    record Result(Long id, String calendarUrl) {}
    Result create(Command command);
}
```

**예외 처리 패턴** — 구체 예외 클래스 없이 `EcoSyncException(ErrorCode)` 직접 사용:

```java
// ErrorCode에 HTTP 상태코드, 에러 코드 문자열, 메시지 일괄 정의
throw new EcoSyncException(ErrorCode.SUBSCRIPTION_NOT_FOUND);
```

- `ErrorCode` — `HttpStatus` + code + message 보유 (`com.ecosync.application.exception`)
- `EcoSyncException` — ErrorCode를 받는 단일 예외 클래스
- `GlobalExceptionHandler` (api 레이어) — `EcoSyncException` → `ErrorResponse` 변환
- `ErrorResponse` (api 레이어 DTO) — `{ code, message }` 형태

### Infrastructure 레이어

**JPA 엔티티 기반 클래스 상속 계층:**

```
BaseCreatedEntity   (@CreatedBy + createdAt)
  └─ BaseUpdatedEntity   (+ @LastModifiedBy + updatedAt)
       └─ BaseSoftDeleteEntity   (+ deletedBy + deletedAt, softDelete(), restore(), isActive())
```

- `@MappedSuperclass` + `@Getter` (`@SuperBuilder` 불필요 — Hibernate 자동 관리)
- `@EntityListeners(AuditingEntityListener.class)`는 `BaseCreatedEntity`에만 선언
- `@CreatedBy`는 `updatable = false` 추가
- 모든 컬럼에 `@Comment("설명")` 추가

**엔티티 클래스:**

```java
@Entity @Table(name = "테이블명")
@Getter @Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class XxxEntity extends BaseSoftDeleteEntity {   // soft delete 없으면 BaseUpdatedEntity

    @Comment("설명")
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Comment("설명")
    @Enumerated(EnumType.STRING)   // enum 필드는 반드시 STRING
    @Column(...)
    private SomeEnum someEnum;
}
```

- 클래스명: `XxxEntity` (`XxxJpaEntity` 사용 안 함)

**레포지토리:**

- 위치: `com.ecosync.infrastructure.persistence.repository`, 접근 제어 `public`
- 이름: `XxxJpaRepository`
- soft delete JPQL은 `@Modifying @Query`로 정의

**MapStruct 매퍼:**

- 메서드 네이밍: `toDomain()`, `toEntity()`, `xyzToDomain()`, `xyzToEntity()`
- 상속 필드 `ignore` 선언 불필요 — MapStruct 자동 처리

```java
@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    Subscription toDomain(SubscriptionEntity entity);
    SubscriptionEntity toEntity(Subscription domain);
    @Mapping(source = "subscription.id", target = "subscriptionId")
    SubscriptionInterest interestToDomain(SubscriptionInterestEntity entity);
    @Mapping(source = "subscriptionId", target = "subscription.id")
    SubscriptionInterestEntity interestToEntity(SubscriptionInterest domain);
}
```

**어댑터:**

- 클래스명: `XxxAdapter`, `@Repository`로 Output Port 구현
- 신규/기존 분기: `id == null`이면 `mapper.toEntity()`, 기존이면 JPA 엔티티 로드 후 비즈니스 메서드 호출

```java
if (domain.getId() == null) {
    entity = mapper.toEntity(domain);
} else {
    entity = repository.findById(domain.getId()).orElseThrow(...);
    if (domain.isActive()) entity.restore();
    else entity.softDelete();
}
```

### API 레이어

패키지 구조:
```
com.ecosync.api/
├── config/       # SecurityConfig, WebConfig, JpaConfig, SwaggerConfig
├── controller/   # XxxController, GlobalExceptionHandler
└── dto/
    ├── request/  # XxxRequest (record + @Valid 제약)
    └── response/ # XxxResponse (record), ErrorResponse
```

- `ErrorResponse` 사용: 모든 에러 응답은 `GlobalExceptionHandler`를 통해 `{ code, message }` 형태로 반환
- `@Valid` 검증 실패 → `COMMON_001` 코드로 필드 오류 메시지 반환

### QueryDSL

- **라이브러리**: `io.github.openfeign.querydsl` (OpenFeign fork) 7.1 — Jakarta EE 네이티브, classifier 불필요
- **위치**: `eco-sync-infrastructure` 모듈에만 설정
- **Q-클래스 생성 위치**: `build/generated/sources/annotationProcessor/java/main/`
- Gradle 의존성:

```groovy
implementation "io.github.openfeign.querydsl:querydsl-jpa:7.1"
annotationProcessor "io.github.openfeign.querydsl:querydsl-apt:7.1:jpa"
annotationProcessor 'jakarta.persistence:jakarta.persistence-api'
annotationProcessor 'jakarta.annotation:jakarta.annotation-api'
```

- `JPAQueryFactory`는 인프라 모듈 Config 또는 어댑터에서 `EntityManager`를 주입받아 사용

### JPA / Auditing 설정

**Spring Boot 4.x 유의사항:**

- `@EntityScan` 패키지: `org.springframework.boot.persistence.autoconfigure.EntityScan`
  (기존 `org.springframework.boot.autoconfigure.domain.EntityScan` 삭제됨)
- `scanBasePackages`는 컴포넌트 스캔만 설정 — JPA 레포지토리·엔티티 스캔은 `@EnableJpaRepositories` / `@EntityScan` 별도 필요

**JpaConfig 분리 원칙:** api/batch 모듈 각각에 정의. 인프라 모듈에 두지 않는다.

```java
@Configuration
@EnableJpaRepositories(basePackages = "com.ecosync.infrastructure.persistence.repository")
@EntityScan(basePackages = "com.ecosync.infrastructure.persistence.entity")
@EnableJpaAuditing
public class JpaConfig {
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("test");   // batch는 "system"
    }
}
```

