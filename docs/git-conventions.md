# Git 컨벤션

---

## 브랜치 전략

```
main ← dev ← feature/*
              bugfix/*
              chore/*
              docs/*
              infra/*
```

- `main`: 프로덕션 브랜치
- `dev`: 개발 통합 브랜치
- 모든 작업은 `dev`에서 분기, `dev`로 머지

---

## 브랜치 명명 규칙

```
feature/description   # 새 기능
bugfix/description    # 버그 수정
chore/description     # 리팩토링, 의존성 업데이트 등
docs/description      # 문서화
infra/description     # 인프라/배포
```

---

## 커밋 메시지

```
[TYPE] 제목

본문 (선택)
```

**TYPE 종류**

| TYPE | 설명 |
|---|---|
| `feat` | 새로운 기능 |
| `fix` | 버그 수정 |
| `refactor` | 코드 리팩토링 |
| `docs` | 문서 |
| `infra` | 인프라/배포/SQL |
| `chore` | 빌드, 의존성, 설정 변경 |
| `test` | 테스트 추가/수정 |

**예시**

```
[feat] 구독 등록 API 구현

[fix] MySQL 한글 데이터 깨짐 현상 해결

[infra] SQL 스키마 audit 컬럼 추가
```

---

## 커밋 그룹핑 원칙

- **한 커밋에 한 가지 관심사**
- SQL/인프라 변경은 함께 커밋
- 백엔드는 레이어별로 분리 커밋 (domain / application / infrastructure / config)
- 연관 없는 변경사항은 섞지 않음

---

## PR 규칙

- 베이스 브랜치: `dev`
- 제목: `[CATEGORY][TYPE] 작업 요약`
- Squash Merge 사용

**CATEGORY 종류**

| CATEGORY | 설명 |
|---|---|
| `BE` | 백엔드 |
| `FE` | 프론트엔드 |
| `INFRA` | 인프라/배포 |
| `DOCS` | 문서/기획 |

**예시**

```
[BE][feat] Phase 1 구독 API 구현
[FE][feat] 국가 선택 화면 구현
[INFRA][chore] Docker Compose MySQL 설정 추가
[DOCS] DB 스키마 설계
```
