-- EcoSync DB Schema
-- Phase 1: 국가별 증시 휴장일 ICS 구독
-- Phase 2: 경제지표 발표일 (예정)
-- Phase 3: 기업 실적 발표일 (예정)

-- ──────────────────────────────────────────────
-- 1. subscriptions
--    구독자. calendar_token(UUID)으로 ICS URL 생성.
--    재구독 시 deleted_at을 NULL로 복원해 재활성화.
-- ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS subscriptions
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    email          VARCHAR(255) NOT NULL,
    calendar_token CHAR(36)     NOT NULL COMMENT 'UUID — ICS URL 식별자',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at     DATETIME     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_subscriptions_email (email),
    UNIQUE KEY uq_subscriptions_token (calendar_token)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────
-- 2. subscription_interests
--    구독 관심사. interest_type + interest_value 조합으로 Phase 1~3 커버.
--
--    Phase 1  COUNTRY    / KR, US, JP, CN, HK, GB, DE
--    Phase 2  IMPORTANCE / HIGH, MEDIUM, LOW
--    Phase 3  TICKER     / AAPL, TSLA, 005930 ...
--
--    subscription_interests ↔ economic_events 간 DB FK 없음.
--    ICS 생성 시 category + interest_value 기준으로 애플리케이션에서 필터링.
-- ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS subscription_interests
(
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    subscription_id BIGINT      NOT NULL,
    interest_type   VARCHAR(20) NOT NULL COMMENT 'COUNTRY | IMPORTANCE | TICKER',
    interest_value  VARCHAR(50) NOT NULL COMMENT 'KR, US | HIGH, MEDIUM | AAPL',
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      DATETIME    NULL,
    PRIMARY KEY (id),
    KEY idx_si_subscription_id (subscription_id),
    CONSTRAINT fk_si_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────
-- 3. economic_events
--    경제 이벤트 마스터. uid(iCal UID)로 upsert 처리.
--
--    category별 사용 필드:
--      HOLIDAY    — country_code, exchange, is_closed, (event_time NULL)
--      INDICATOR  — country_code, importance, event_time
--      EARNINGS   — country_code, ticker, event_time
--
--    event_date는 UTC 기준.
--    배치가 uid 기준으로 upsert하므로 uid는 UNIQUE.
-- ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS economic_events
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    uid          VARCHAR(255) NOT NULL COMMENT 'iCal UID — 배치 upsert 기준 키',
    title        VARCHAR(255) NOT NULL,
    event_date   DATE         NOT NULL COMMENT 'UTC 기준 날짜',
    event_time   TIME         NULL     COMMENT 'NULL = 종일 이벤트',
    country_code VARCHAR(10)  NOT NULL,
    exchange     VARCHAR(20)  NULL     COMMENT 'KRX, NYSE 등 — HOLIDAY 전용',
    ticker       VARCHAR(20)  NULL     COMMENT 'AAPL, 005930 등 — EARNINGS 전용',
    category     VARCHAR(20)  NOT NULL COMMENT 'HOLIDAY | INDICATOR | EARNINGS',
    importance   VARCHAR(10)  NULL     COMMENT 'HIGH | MEDIUM | LOW — INDICATOR 전용',
    is_closed    BOOLEAN      NULL     COMMENT '완전 휴장 여부 — HOLIDAY 전용',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_ee_uid (uid),
    KEY idx_ee_category_country_date (category, country_code, event_date),
    KEY idx_ee_country_date (country_code, event_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
