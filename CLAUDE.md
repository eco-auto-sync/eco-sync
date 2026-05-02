# EcoSync

사용자가 관심 있는 경제 일정(국가별 증시 휴장일, 경제지표 발표일, 기업 실적 발표일)을 선택하면
Google Calendar 등 외부 캘린더 앱에서 ICS 구독으로 자동 확인할 수 있는 서비스.

---

## 작업 범위별 참고 문서

- **백엔드 작업** → `backend/CLAUDE.md` 참고
- **프론트엔드 작업** → `frontend/CLAUDE.md` 참고

---

## 모노레포 구조

```
eco-sync/
├── backend/    # Spring Boot — API 서버 + Batch 서버 (헥사고날 멀티모듈)
├── frontend/   # Next.js 16 — React + TypeScript + Tailwind CSS
├── infra/      # 공통 인프라 (Docker Compose — MySQL, phpMyAdmin)
└── docs/       # 프로젝트 문서
```

```
docs/
├── phase1-spec.md        # Phase 1 기능 명세서 (화면·API·배치·데이터)
├── fmp-api.md            # Financial Modeling Prep API 정리
└── backend-conventions.md  # 백엔드 코딩 컨벤션
```

---

## 개발 로드맵

| Phase | 기간 | 목표 |
|---|---|---|
| **Phase 1** (Now) | 2026.03.10 ~ 04.05 | 국가별 증시 휴장일 ICS 구독 MVP |
| **Phase 2** (Next) | 2026.04.06 ~ 05.17 | 금리·CPI·GDP 등 경제지표 캘린더 연동 |
| **Phase 3** (Later) | 2026.05.18 ~ 06.28 | 기업 실적 발표일 구독 + D-3 사전 알림 |

Phase 2, 3은 Phase 1의 ICS 인프라(토큰 기반 구독 URL) 위에 빌드됨.

---

## 로컬 인프라 실행

```bash
cd infra/local
cp .env.example .env   # MYSQL_ROOT_PASSWORD, MYSQL_USER, MYSQL_PASSWORD, MYSQL_DATABASE 설정
docker-compose up -d   # MySQL 3306, phpMyAdmin 8888
```
