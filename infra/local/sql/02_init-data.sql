-- EcoSync Initial Data
-- Sample data for local development environment

-- =====================================================
-- Sample Subscriptions
-- =====================================================
INSERT INTO subscriptions (email, calendar_token) VALUES
('user1@example.com', '550e8400-e29b-41d4-a716-446655440001'),
('user2@example.com', '550e8400-e29b-41d4-a716-446655440002');

-- =====================================================
-- Sample Subscription Interests (Phase 1: COUNTRY)
-- =====================================================
INSERT INTO subscription_interests (subscription_id, interest_type, interest_value) VALUES
(1, 'COUNTRY', 'KR'),
(1, 'COUNTRY', 'US'),
(2, 'COUNTRY', 'JP'),
(2, 'COUNTRY', 'US');

-- =====================================================
-- Sample Economic Events (Phase 1: HOLIDAY)
-- =====================================================
INSERT INTO economic_events (uid, title, event_date, country_code, exchange, category, is_closed) VALUES
('KRX-2026-01-01', '신정', '2026-01-01', 'KR', 'KRX', 'HOLIDAY', true),
('KRX-2026-01-27', '설날 연휴', '2026-01-27', 'KR', 'KRX', 'HOLIDAY', true),
('KRX-2026-01-28', '설날', '2026-01-28', 'KR', 'KRX', 'HOLIDAY', true),
('KRX-2026-01-29', '설날 연휴', '2026-01-29', 'KR', 'KRX', 'HOLIDAY', true),
('NYSE-2026-01-01', "New Year's Day", '2026-01-01', 'US', 'NYSE', 'HOLIDAY', true),
('NYSE-2026-01-20', 'Martin Luther King Jr. Day', '2026-01-20', 'US', 'NYSE', 'HOLIDAY', true),
('NYSE-2026-02-16', "Presidents' Day", '2026-02-16', 'US', 'NYSE', 'HOLIDAY', true);
