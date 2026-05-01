# Financial Modeling Prep (FMP) API

EcoSync에서 경제 일정 데이터 수집에 사용하는 외부 API.

- 공식 문서: https://site.financialmodelingprep.com/developer/docs
- 인증: 모든 요청에 API 키 필요
  - Query param: `?apikey=YOUR_KEY`
  - Header: `apikey: YOUR_KEY`

---

## 요금제

| 플랜 | 가격 | 호출 제한 | 커버리지 |
|---|---|---|---|
| Basic | 무료 | 250회/일 | 기본 |
| Starter | $19/월 | 300회/분 | US |
| Premium | $49/월 | 750회/분 | US, UK, Canada |
| Ultimate | $99/월 | 3,000회/분 | 글로벌 전체 |

> 배치로 주 1회 수집하면 Basic(무료)으로 충분. 서비스 규모 커지면 Starter 이상 검토.

---

## Phase 1 — 휴장일

### 거래소 목록 조회

국가/거래소 선택 화면에서 지원 거래소 목록을 가져올 때 사용.

```
GET https://financialmodelingprep.com/stable/all-exchange-market-hours
```

**응답 예시**
```json
[
  {
    "exchange": "KRX",
    "name": "Korea Exchange",
    "openingHour": "09:00 AM +09:00",
    "closingHour": "03:30 PM +09:00",
    "timezone": "Asia/Seoul",
    "isMarketOpen": false
  }
]
```

### 거래소별 휴장일 조회

구독한 거래소의 휴장일 데이터를 수집할 때 사용.

```
GET https://financialmodelingprep.com/stable/holidays-by-exchange
```

**파라미터**

| 파라미터 | 타입 | 필수 | 예시 | 설명 |
|---|---|---|---|---|
| `exchange` | string | ✅ | `KRX` | 거래소 코드 |
| `from` | date | | `2025-01-01` | 조회 시작일 |
| `to` | date | | `2025-12-31` | 조회 종료일 |

**응답 예시**
```json
[
  {
    "exchange": "KRX",
    "date": "2025-01-01",
    "name": "New Year's Day",
    "isClosed": true,
    "adjOpenTime": null,
    "adjCloseTime": null
  }
]
```

**주요 필드**
- `isClosed`: 완전 휴장 여부. `false`이면 단축 거래일 (adjOpenTime/adjCloseTime 참고)
- `adjOpenTime` / `adjCloseTime`: 단축 거래 시간 (휴장이 아닌 경우)

---

## 국가 → 거래소 매핑 (주요)

FMP API는 국가 단위가 아닌 거래소 단위로 동작. 사용자에게 국가로 보여주고 내부에서 매핑.

| 국가 | 거래소 코드 | 거래소명 |
|---|---|---|
| 한국 | KRX | Korea Exchange |
| 미국 | NYSE | New York Stock Exchange |
| 미국 | NASDAQ | NASDAQ |
| 일본 | JPX | Japan Exchange Group |
| 중국 | SHSE | Shanghai Stock Exchange |
| 홍콩 | HKEX | Hong Kong Exchanges |
| 유럽(독) | XETRA | Deutsche Boerse |
| 영국 | LSE | London Stock Exchange |

> 전체 거래소 목록은 `/stable/all-exchange-market-hours` 응답 참고.

---

## Phase 2 — 경제지표 (예정)

추후 조사 필요. FMP의 Economic Calendar API 활용 예정.

## Phase 3 — 기업 실적 (예정)

추후 조사 필요. FMP의 Earnings Calendar API 활용 예정.
