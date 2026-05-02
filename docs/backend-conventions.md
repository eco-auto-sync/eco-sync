# Backend 개발 컨벤션

헥사고날 아키텍처 기반 백엔드 코드 작성 시 따라야 할 레이어별 컨벤션.

---

## 모듈 의존성 규칙

```
eco-sync-domain        순수 Java — Spring/JPA 의존 없음
eco-sync-application   UseCase(Input Port) + Output Port 인터페이스 — Spring Context만 허용
eco-sync-infrastructure Output Adapter 구현 (JPA, 외부 API, ICS)
eco-sync-api           REST Controller + API 서버 Bootstrap
eco-sync-batch         Spring Batch Job + Batch 서버 Bootstrap
```

- 의존성 방향: 항상 바깥 → 안쪽. `api` ↔ `batch` 상호 참조 금지.
- Gradle 의존성 설정: **`api` 사용 금지** — 모두 `implementation`
- api/batch 모듈에서 JPA 애노테이션이 필요하면 `spring-boot-starter-data-jpa`를 해당 모듈에 직접 선언

---

## Domain 레이어

### 기반 클래스 상속 계층

```
BaseCreated   (createdAt)
  └─ BaseUpdated   (+ updatedAt)
       └─ BaseSoftDelete   (+ deletedAt, softDelete(), restore(), isActive())
```

- `@SuperBuilder` + `@NoArgsConstructor` + `@Getter` 세트 사용
- `@SuperBuilder`가 필요한 이유: MapStruct `toDomain()` 시 상속된 필드까지 빌더에 포함해야 함
- 소프트 딜리트 메서드명: `softDelete()` / `restore()` (JPA 엔티티와 통일)

### 도메인 클래스

```java
@Getter @SuperBuilder @NoArgsConstructor
public class Subscription extends BaseSoftDelete {
    private Long id;
    private String email;
    private String calendarToken;

    public static Subscription create(String email) { ... }  // 팩토리 메서드
}
```

---

## Application 레이어

### Output Port 인터페이스

- 위치: `com.ecosync.application.port.out`
- 도메인 객체만 파라미터/반환 타입으로 사용 (JPA 엔티티 노출 금지)
- Spring 의존: `@Service`, `@Transactional` 허용 / JPA, WebClient 금지

---

## Infrastructure 레이어

### JPA 엔티티 기반 클래스 상속 계층

```
BaseCreatedEntity   (@CreatedBy + createdAt)
  └─ BaseUpdatedEntity   (+ @LastModifiedBy + updatedAt)
       └─ BaseSoftDeleteEntity   (+ deletedBy + deletedAt, softDelete(), restore(), isActive())
```

- `@MappedSuperclass` + `@Getter` 사용 (`@SuperBuilder` 불필요 — Hibernate 자동 관리)
- `@EntityListeners(AuditingEntityListener.class)`는 `BaseCreatedEntity`에만 선언
- `@CreatedBy`는 `updatable = false` 추가
- 모든 컬럼에 `@Comment("설명")` 추가

### 엔티티 클래스

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
- `economic_events`처럼 soft delete 없는 테이블: `BaseUpdatedEntity` 상속

### 레포지토리

- 위치: `com.ecosync.infrastructure.persistence.repository`
- 인터페이스 접근 제어: `public` (어댑터와 패키지가 다르므로)
- 이름: `XxxJpaRepository`
- soft delete JPQL은 레포지토리에 `@Modifying @Query`로 정의

### MapStruct 매퍼

- 메서드 네이밍: `toDomain()`, `toEntity()`, `xyzToDomain()`, `xyzToEntity()`
- 중첩 필드 매핑: `@Mapping(source = "subscription.id", target = "subscriptionId")`
- 상속 필드(`createdAt` 등) `ignore` 선언 불필요 — MapStruct가 자동 처리

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

### 어댑터

- 클래스명: `XxxAdapter`, `@Repository` 애노테이션으로 Output Port 구현
- 신규/기존 분기 패턴: `id == null`이면 `mapper.toEntity()`, 기존이면 JPA 엔티티 로드 후 비즈니스 메서드 호출

```java
if (domain.getId() == null) {
    entity = mapper.toEntity(domain);
} else {
    entity = repository.findById(domain.getId()).orElseThrow(...);
    if (domain.isActive()) entity.restore();
    else entity.softDelete();
}
```

---

## JPA / Auditing 설정

### Spring Boot 4.x 유의사항

- `@EntityScan` 패키지: `org.springframework.boot.persistence.autoconfigure.EntityScan`
  (기존 `org.springframework.boot.autoconfigure.domain.EntityScan` 삭제됨)
- `@SpringBootApplication(scanBasePackages = "com.ecosync")`은 컴포넌트 스캔 범위만 지정 —
  JPA 레포지토리·엔티티 스캔은 별도 `@EnableJpaRepositories` / `@EntityScan` 필요

### JpaConfig 분리 원칙

`@EnableJpaRepositories`, `@EntityScan`, `@EnableJpaAuditing`, `AuditorAware` 빈을
**api/batch 모듈 각각의 `JpaConfig`에 정의**. 인프라 모듈에 두지 않는다.

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

- `AuditorAware` 현재 값: api=`"test"`, batch=`"system"` — 추후 Spring Security 연동 예정

---

## SQL 스키마 컨벤션

- 모든 컬럼에 `COMMENT '설명'`
- audit 컬럼: `created_by`, `created_at`, `updated_by`, `updated_at` 전 테이블 공통
- soft delete 테이블 추가 컬럼: `deleted_by`, `deleted_at`
- soft delete: `deleted_at IS NULL` = 활성, NULL 복원으로 재활성화
- `economic_events`는 soft delete 없음 — `uid` 기준 upsert로 관리
- `subscription_interests` ↔ `economic_events` 간 DB FK 없음 — 앱 레벨에서 필터링
- DDL 위치: `infra/local/sql/01_schema.sql`
