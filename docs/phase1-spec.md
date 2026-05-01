# Phase 1 기능 명세서 — 관심 국가별 증시 휴장일 구독

## 서비스 개요

이메일을 입력하고 관심 국가를 선택하면, 해당 국가들의 증시 휴장일이 담긴 ICS 구독 URL을 발급해준다.
사용자는 이 URL을 Google Calendar 등에 등록하면 휴장일을 자동으로 확인할 수 있다.

---

## 화면 플로우

```
랜딩 페이지
    ↓ 시작하기
국가 선택 페이지 (이메일 입력 + 국가 다중 선택)
    ↓ 구독 생성
URL 발급 페이지 (ICS URL 표시 + 캘린더 등록 가이드)
```

---

## 화면별 기능 명세

### 1. 랜딩 페이지 (`/`)

- 서비스 소개 문구
- "지금 시작하기" 버튼 → 국가 선택 페이지 이동
- 기존 구독자 재조회 링크 ("이미 구독 중이라면?") → 이메일 입력 후 URL 재조회

---

### 2. 국가 선택 페이지 (`/subscribe`)

**이메일 입력**
- 이메일 형식 유효성 검사
- 기존 구독이 있는 이메일이면 → 기존 구독 정보 불러와서 수정 모드로 진입

**국가 선택**
- 지원 국가 카드 목록 표시 (다중 선택)
- 최소 1개 이상 선택 필수

**지원 국가 목록 (Phase 1)**

| 국가 | 거래소 코드 |
|---|---|
| 🇰🇷 한국 | KRX |
| 🇺🇸 미국 | NYSE, NASDAQ |
| 🇯🇵 일본 | JPX |
| 🇨🇳 중국 | SHSE |
| 🇭🇰 홍콩 | HKEX |
| 🇬🇧 영국 | LSE |
| 🇩🇪 독일 | XETRA |

**구독 생성 / 수정 버튼**
- 신규: "구독 URL 발급받기"
- 수정: "구독 업데이트"

---

### 3. URL 발급 페이지 (`/subscribe/complete`)

**발급된 ICS URL 표시**
- URL 복사 버튼
- Google Calendar 바로 추가 버튼 (webcal:// 프로토콜 활용)

**캘린더 등록 가이드**
- Google Calendar 등록 방법 안내 (간단한 텍스트 또는 이미지)

**구독 관리 링크**
- "구독 수정" → 국가 선택 페이지 (수정 모드)
- "구독 취소" → 확인 후 삭제

---

### 4. 구독 재조회 (`/my`)

- 이메일 입력 → 해당 이메일의 구독 정보 조회
- 구독이 없으면 → 국가 선택 페이지로 안내
- 구독이 있으면 → URL 발급 페이지로 이동 (URL + 수정/취소 옵션)

---

## API 명세

### 국가 목록 조회
```
GET /api/countries
Response: [{ "code": "KR", "name": "한국", "exchange": "KRX", "flag": "🇰🇷" }]
```

### 구독 생성
```
POST /api/subscriptions
Body: { "email": "user@example.com", "countryCodes": ["KR", "US"] }
Response: { "id": 1, "calendarUrl": "webcal://ecosync.com/api/calendar/{token}/subscribe" }
```

### 구독 재조회
```
GET /api/subscriptions?email=user@example.com
Response: { "id": 1, "email": "...", "countryCodes": ["KR", "US"], "calendarUrl": "..." }
```

### 구독 수정
```
PUT /api/subscriptions/{id}
Body: { "countryCodes": ["KR", "US", "JP"] }
Response: { "id": 1, "calendarUrl": "..." }
```

### 구독 취소
```
DELETE /api/subscriptions/{id}
Response: 204 No Content
```

### ICS 파일 스트리밍
```
GET /api/calendar/{token}/subscribe
Response: ICS 파일 (Content-Type: text/calendar)
```

---

## 데이터 명세

### 구독 (Subscription)
| 필드 | 타입 | 설명 |
|---|---|---|
| id | Long | PK |
| email | String | 사용자 이메일 |
| calendarToken | UUID | ICS URL 식별자 |
| countryCodes | List\<String\> | 구독 국가 코드 목록 |
| createdAt | Timestamp | 생성일시 |

### 휴장일 (EconomicEvent)
| 필드 | 타입 | 설명 |
|---|---|---|
| uid | String | iCal 중복 방지용 고유 ID |
| title | String | 일정 제목 (예: 설날) |
| eventDatetime | Datetime | 휴장일 (UTC) |
| countryCode | String | 국가 코드 |
| exchange | String | 거래소 코드 |
| category | String | HOLIDAY 고정 |

---

## 배치 명세

### 휴장일 수집 배치
- **주기**: 매일 새벽 2시
- **수집 범위**: 오늘 기준 ~2년치
- **대상**: 지원 국가 7개 거래소 전체
- **외부 API**: FMP `/stable/holidays-by-exchange`
- **중복 처리**: `uid` 기준 upsert

---

## 제외 범위 (Phase 1)

- 회원가입 / 로그인 (이메일만 사용)
- 알림 기능
- 경제지표 / 기업 실적 (Phase 2, 3)
- 관리자 화면
