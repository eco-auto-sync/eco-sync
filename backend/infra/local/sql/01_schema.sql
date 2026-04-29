-- EcoSync Database Schema
-- Created for local development environment
-- Based on eco-auto-sync ERD Design

-- =====================================================
-- Users Table
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 ID',
    email VARCHAR(255) NOT NULL UNIQUE COMMENT '사용자 이메일',
    calendar_token VARCHAR(36) NOT NULL UNIQUE COMMENT 'iCal 고유 토큰 (UUID)',
    created_by VARCHAR(255) COMMENT '생성자 (email 또는 user_id)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_by VARCHAR(255) COMMENT '수정자 (email 또는 user_id)',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    deleted_at TIMESTAMP NULL COMMENT '삭제 일시 (소프트 딜리트)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 정보';

-- =====================================================
-- User Interests Table
-- =====================================================
CREATE TABLE IF NOT EXISTS user_interests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 관심사 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    interest_type VARCHAR(50) NOT NULL COMMENT '설정 타입 (COUNTRY, TICKER, IMPORTANCE)',
    interest_value VARCHAR(100) NOT NULL COMMENT '설정 값 (예: US, NVDA, HIGH)',
    created_by VARCHAR(255) COMMENT '생성자 (email 또는 user_id)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_by VARCHAR(255) COMMENT '수정자 (email 또는 user_id)',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    deleted_at TIMESTAMP NULL COMMENT '삭제 일시 (소프트 딜리트)',
    UNIQUE KEY uk_user_interest_type_value (user_id, interest_type, interest_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 구독 설정';

-- =====================================================
-- Economic Events Table
-- =====================================================
CREATE TABLE IF NOT EXISTS economic_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '경제 일정 ID',
    uid VARCHAR(255) NOT NULL UNIQUE COMMENT 'iCal 고유 식별자 (원천 데이터의 고유 ID)',
    title VARCHAR(255) NOT NULL COMMENT '일정 제목 (예: 미국 CPI 발표)',
    event_datetime DATETIME NOT NULL COMMENT '일정 일시 (UTC 기준)',
    country_code VARCHAR(10) COMMENT '국가 코드 (예: KR, US)',
    category VARCHAR(50) COMMENT '카테고리 (예: INDICATOR, EARNINGS)',
    ticker VARCHAR(50) COMMENT '종목 코드 (예: AAPL, 005930)',
    importance VARCHAR(20) COMMENT '중요도 (LOW, MID, HIGH)',
    description LONGTEXT COMMENT '상세 내용 (예상치, 이전치 등)',
    created_by VARCHAR(255) COMMENT '생성자 (email 또는 user_id)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_by VARCHAR(255) COMMENT '수정자 (email 또는 user_id)',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    deleted_at TIMESTAMP NULL COMMENT '삭제 일시 (소프트 딜리트)',
    UNIQUE KEY uk_uid (uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='경제 일정 마스터 테이블';
