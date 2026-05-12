# EcoSync Infra

로컬 개발 환경 인프라. Docker Compose 기반 MySQL + phpMyAdmin.

---

## 구조

```
infra/
└── local/
    ├── .env                  # 환경 변수 (git 제외)
    ├── docker-compose.yaml   # MySQL 8.0 + phpMyAdmin
    ├── my.cnf                # MySQL 문자셋 설정 (utf8mb4)
    └── sql/
        ├── 01_schema.sql     # DDL — 컨테이너 최초 기동 시 자동 실행
        └── 02_init-data.sql  # 로컬 개발용 샘플 데이터
```

---

## 실행

```bash
cd infra/local
cp .env.example .env   # 최초 1회
docker-compose up -d
docker-compose down    # 중지
docker-compose down -v # 중지 + 볼륨 삭제 (DB 초기화)
```

### 접속 정보

| 항목 | 값 |
|---|---|
| MySQL | `localhost:3306` |
| phpMyAdmin | `http://localhost:8888` |
| DB 이름 | `.env`의 `MYSQL_DATABASE` |
| 사용자 | `.env`의 `MYSQL_USER` / `MYSQL_PASSWORD` |

---

## 환경 변수 (`.env`)

```dotenv
MYSQL_ROOT_PASSWORD=
MYSQL_USER=ecosync_user
MYSQL_PASSWORD=
MYSQL_DATABASE=ecosync_dev
```

---

## SQL 파일 관리 규칙

- `01_schema.sql` — DDL만. 테이블 생성/변경. `CREATE TABLE IF NOT EXISTS` 사용.
- `02_init-data.sql` — 로컬 개발용 샘플 데이터만. 운영 데이터 절대 포함 금지.
- 파일은 컨테이너 **최초 기동 시 한 번만** 실행됨. 스키마 변경 후 반영하려면 `down -v` 후 재기동.
- 파일명 숫자 prefix로 실행 순서 보장 (`01_` → `02_` 순).

### 컬럼 작성 규칙

- 모든 컬럼에 `COMMENT '설명'`
- audit 컬럼 순서: `created_by`, `created_at`, `updated_by`, `updated_at`, `deleted_at`
- soft delete 적용 테이블: `deleted_at DATETIME NULL` (NULL = 활성)
- 기본값: `created_at DEFAULT CURRENT_TIMESTAMP`, `updated_at ON UPDATE CURRENT_TIMESTAMP`

---

## MySQL 설정 (`my.cnf`)

- 문자셋: `utf8mb4` / `utf8mb4_unicode_ci` 전역 적용
- 인증: `mysql_native_password`
