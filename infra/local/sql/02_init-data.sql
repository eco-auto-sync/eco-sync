-- EcoSync Initial Data
-- Sample data for local development environment
-- Based on eco-auto-sync ERD Design

-- =====================================================
-- Sample Users
-- =====================================================
INSERT INTO users (email, calendar_token) VALUES
('user1@example.com', '550e8400-e29b-41d4-a716-446655440001'),
('user2@example.com', '550e8400-e29b-41d4-a716-446655440002'),
('user3@example.com', '550e8400-e29b-41d4-a716-446655440003');

-- =====================================================
-- Sample User Interests
-- =====================================================
INSERT INTO user_interests (user_id, interest_type, interest_value) VALUES
-- User 1: 미국과 한국 경제 지표 구독
(1, 'COUNTRY', 'US'),
(1, 'COUNTRY', 'KR'),
(1, 'IMPORTANCE', 'HIGH'),
-- User 2: 애플 및 삼성 기업실적 구독
(2, 'TICKER', 'AAPL'),
(2, 'TICKER', '005930'),
(2, 'IMPORTANCE', 'HIGH'),
(2, 'IMPORTANCE', 'MID'),
-- User 3: 전체 중요도 높은 이벤트 구독
(3, 'IMPORTANCE', 'HIGH');

-- =====================================================
-- Sample Economic Events
-- =====================================================
INSERT INTO economic_events (uid, title, event_datetime, country_code, category, ticker, importance, description) VALUES
('eco-event-001', '미국 CPI 발표 (2월)', '2026-03-10 14:30:00', 'US', 'INDICATOR', NULL, 'HIGH', '예상치: 2.4% YoY | 이전치: 2.6% YoY'),
('eco-event-002', '미국 실업률 발표 (2월)', '2026-03-06 13:30:00', 'US', 'INDICATOR', NULL, 'HIGH', '예상치: 4.0% | 이전치: 3.9%'),
('eco-event-003', '한국 수출입 통계 (2월)', '2026-03-01 10:00:00', 'KR', 'INDICATOR', NULL, 'MID', '전월 대비 변화율 예상'),
('eco-event-004', 'Apple Q2 2026 실적 발표', '2026-04-28 16:30:00', 'US', 'EARNINGS', 'AAPL', 'HIGH', '매출, 영업이익, EPS 공시'),
('eco-event-005', '삼성전자 2026년 1분기 실적', '2026-04-09 15:00:00', 'KR', 'EARNINGS', '005930', 'HIGH', '반도체 부문 수익률 기대'),
('eco-event-006', 'NVIDIA Q1 2026 실적 발표', '2026-05-22 16:30:00', 'US', 'EARNINGS', 'NVDA', 'HIGH', 'AI칩 수요 동향 주목'),
('eco-event-007', '미국 연방준비제도 정책결정회의', '2026-03-19 18:00:00', 'US', 'MONETARY_POLICY', NULL, 'HIGH', '기준금리 결정 및 성명문 공시'),
('eco-event-008', '유럽중앙은행 정책결정회의', '2026-03-12 13:45:00', 'EU', 'MONETARY_POLICY', NULL, 'HIGH', '기준금리 결정'),
('eco-event-009', '한국 생산자물가지수 (2월)', '2026-03-04 10:00:00', 'KR', 'INDICATOR', NULL, 'MID', '전월 대비 변화율'),
('eco-event-010', '일본 수출 (2월)', '2026-03-13 08:50:00', 'JP', 'INDICATOR', NULL, 'MID', '전년 대비 증감률 예상: -2.5%');
