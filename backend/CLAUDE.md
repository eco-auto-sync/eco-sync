# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소에서 작업할 때 참고하는 개발 가이드입니다.

---

## 1. 프로젝트 개요

**EcoSync Backend**는 Spring Boot 4.0.3와 Java 25를 기반으로 한 백엔드 프로젝트입니다. Hexagonal Architecture (Ports & Adapters) 패턴을 따르는 5개 모듈의 Gradle 멀티모듈 구조로 설계되어 있으며, API 서버와 배치 서버를 독립적으로 실행할 수 있습니다.

- **그룹:** `com.ecosync`
- **버전:** `0.0.1-SNAPSHOT`
- **빌드 도구:** Gradle 9.3.1
- **개발 언어:** Java 25
- **프레임워크:** Spring Boot 4.0.3

---

## 2. 아키텍처 개요

EcoSync Backend는 **Hexagonal Architecture** (육각형 아키텍처)를 따릅니다. 이 패턴은 비즈니스 로직을 외부의 기술 구현으로부터 격리하여 테스트 용이성과 유지보수성을 높입니다.

### 아키텍처 계층 구조

```
┌──────────────────────────────────────────────────────────┐
│                                                            │
│          사용자 요청 (HTTP 요청, CLI 명령)                │
│                    ↓                                       │
│    ┌─────────────────────────────────────┐               │
│    │   API/Batch 모듈 (Input Adapter)    │  REST/Batch  │
│    │  - REST 컨트롤러                   │  인터페이스   │
│    │  - 배치 작업 핸들러                 │               │
│    └─────────────────────────────────────┘               │
│                    ↓                                       │
│    ┌─────────────────────────────────────┐               │
│    │   Application 모듈 (UseCase/Port)   │  비즈니스    │
│    │  - 애플리케이션 비즈니스 로직       │  로직        │
│    │  - Input/Output Port 인터페이스     │               │
│    └─────────────────────────────────────┘               │
│                    ↓                                       │
│    ┌─────────────────────────────────────┐               │
│    │   Domain 모듈 (순수 도메인)         │  핵심        │
│    │  - 도메인 엔티티                   │  도메인      │
│    │  - 값 객체 (Value Objects)         │  모델        │
│    │  - 비즈니스 규칙                   │               │
│    └─────────────────────────────────────┘               │
│                    ↓                                       │
│    ┌─────────────────────────────────────┐               │
│    │   Infrastructure 모듈 (Output)      │  기술        │
│    │  - JPA Repository 구현              │  구현        │
│    │  - 외부 API 연동                    │               │
│    │  - 데이터베이스 어댑터              │               │
│    └─────────────────────────────────────┘               │
│                    ↓                                       │
│        외부 시스템 (DB, 외부 API)                         │
│                                                            │
└──────────────────────────────────────────────────────────┘
```

### 의존성 방향 원칙

- **항상 바깥에서 안쪽으로만 의존**
- Domain은 스프링이나 외부 라이브러리를 모름
- Application은 Infrastructure, API, Batch를 모름
- API와 Batch는 서로를 모름 (순환 의존성 금지)

---

## 3. 모듈별 역할 및 책임

### 3.1 eco-sync-domain (순수 도메인 모델)

**역할:** 비즈니스 도메인의 핵심 개념을 표현하는 순수 Java 모듈

**포함 항목:**
- 도메인 엔티티 (`User`, `UserInterest`, `EconomicEvent` 등)
- 값 객체 (Value Objects)
- 도메인 예외 (`DomainException` 등)

**특징:**
- Spring, JPA 등 외부 라이브러리 의존성 없음
- 순수 Java로만 구성
- 가장 재사용 가능성이 높은 모듈
- 테스트가 가장 간단함 (외부 의존성 없음)

**의존성:** 없음 (단, Lombok 사용 가능)

### 3.2 eco-sync-application (UseCase & Port 인터페이스)

**역할:** 애플리케이션의 비즈니스 로직(UseCase)과 포트(Port) 인터페이스 정의

**포함 항목:**
- Input Port 인터페이스 (UseCase 인터페이스)
- Output Port 인터페이스 (Repository, 외부 API 포트)
- Service 클래스 (UseCase 구현체)

**특징:**
- `@Service`, `@Transactional` 어노테이션 사용 가능 (Spring Context만)
- Domain과 Infrastructure 사이의 계약(Contract) 정의
- 비즈니스 로직의 중심
- 기술 구현(JPA, REST Client 등)을 모름

**의존성:** `eco-sync-domain`

### 3.3 eco-sync-infrastructure (Output Adapter 구현)

**역할:** Application의 Output Port를 구현하는 기술 어댑터

**포함 항목:**
- JPA Repository 구현
- 외부 API 호출 클래스 (WebClient 사용)
- iCal 파일 생성 로직
- MapStruct를 이용한 Entity-DTO 매핑

**특징:**
- JPA, Hibernate, H2/MySQL 등 데이터 접근 기술 포함
- 외부 API 연동 구현
- 기술 결정사항을 모두 포함
- 기술 변경 시에만 수정 (Application 코드는 변경 없음)

**의존성:** `eco-sync-application`, `eco-sync-domain`

**주요 라이브러리:**
- Spring Data JPA
- Hibernate
- MapStruct (Entity-DTO 매핑)
- ical4j (캘린더 파일 생성)
- WebFlux (비동기 HTTP 클라이언트)

### 3.4 eco-sync-api (REST API 서버)

**역할:** HTTP 요청을 처리하는 REST API 서버의 Input Adapter

**포함 항목:**
- REST 컨트롤러 (`@RestController`)
- Request/Response DTO
- 입력 검증 (`@Valid`)
- API 설정 (`SecurityConfig` 등)
- Main 클래스 (`EcoSyncApiApplication`)

**특징:**
- 포트 8080에서 실행
- Spring Security를 통한 인증/인가
- Spring Validation으로 입력 검증
- 독립적으로 빌드/배포 가능 (bootJar)

**의존성:** `eco-sync-application`, `eco-sync-infrastructure`, `eco-sync-domain`

**시작 방법:**
```bash
./gradlew :eco-sync-api:bootRun
```

### 3.5 eco-sync-batch (배치 & 스케줄링 서버)

**역할:** 정기적인 배치 작업 실행을 담당하는 Input Adapter

**포함 항목:**
- Spring Batch Job 구성
- Step 정의
- Quartz 스케줄러 설정
- 배치 설정 클래스
- Main 클래스 (`EcoSyncBatchApplication`)

**특징:**
- 포트 8081에서 실행
- Spring Batch로 대용량 데이터 처리
- Quartz로 정기적 작업 스케줄링
- API와 독립적으로 실행

**의존성:** `eco-sync-application`, `eco-sync-infrastructure`, `eco-sync-domain`

**시작 방법:**
```bash
./gradlew :eco-sync-batch:bootRun
```

---

## 4. 모듈 간 의존성 관계

### 의존성 그래프

```
eco-sync-api (포트 8080)          eco-sync-batch (포트 8081)
         │                                │
         │                                │
         └─────────────┬──────────────────┘
                       │
         ┌─────────────┴──────────────┐
         │                            │
eco-sync-application            eco-sync-infrastructure
(UseCase + Port)                (Adapter 구현)
         │                            │
         └──────────────┬─────────────┘
                        │
                        │
                   eco-sync-domain
                  (순수 도메인 모델)
```

### 의존성 규칙 (필수 준수)

**✅ 허용되는 의존성:**
- API → Application, Infrastructure, Domain
- Batch → Application, Infrastructure, Domain
- Application → Domain
- Infrastructure → Application, Domain

**❌ 금지되는 의존성:**
- Domain → 다른 모듈 (Domain은 의존성 없음)
- Application → Infrastructure, API, Batch
- Infrastructure → API, Batch
- API ↔ Batch (순환 의존성)

### 모듈별 역할 정리

| 모듈 | 역할 | 플러그인 | bootJar | JAR |
|------|------|---------|---------|-----|
| **domain** | 순수 도메인 모델 | `java-library` | ❌ | ✅ |
| **application** | UseCase + Port 인터페이스 | `java-library` | ❌ | ✅ |
| **infrastructure** | Output Adapter 구현 | `java-library` | ❌ | ✅ |
| **api** | REST API 서버 | `org.springframework.boot` | ✅ | ❌ |
| **batch** | 배치 & 스케줄링 서버 | `org.springframework.boot` | ✅ | ❌ |

---

## 5. 프로젝트 구조 (디렉토리 레이아웃)

### 전체 디렉토리 구조

```
eco-sync/backend (루트)
├── settings.gradle                    # 모듈 정의 진입점
├── build.gradle                       # 루트 빌드 설정 (BOM, 공통 플러그인)
├── gradle.properties                  # Gradle 프로퍼티 (Java 버전 등)
├── gradle/
│   ├── wrapper/                       # Gradle Wrapper (gradle-wrapper.jar, gradle-wrapper.properties)
│   └── libs.versions.toml             # Version Catalog (의존성 버전 중앙 관리)
├── gradlew                            # Unix Gradle Wrapper 스크립트
├── gradlew.bat                        # Windows Gradle Wrapper 스크립트
├── CLAUDE.md                          # 개발 가이드 (이 파일)
├── LICENSE                            # 라이센스
├── backend.iml                        # IntelliJ IDEA 프로젝트 파일
│
├── infra/                             # 인프라 및 환경 설정
│   └── local/                         # 로컬 개발 환경
│       ├── docker-compose.yaml        # MySQL 8.0 + PhpMyAdmin Docker 구성
│       ├── .env                       # 환경 변수 (Docker 컨테이너 설정)
│       └── sql/
│           ├── 01_schema.sql          # 데이터베이스 스키마 정의
│           └── 02_init-data.sql       # 샘플 초기 데이터
│
├── env/                               # 환경별 설정 파일 (향후 확장용)
│
├── eco-sync-domain/                   # ① 순수 도메인 모듈 (라이브러리)
│   ├── build.gradle
│   └── src/
│       ├── main/
│       │   ├── java/com/ecosync/domain/
│       │   │   └── (domain 엔티티 및 비즈니스 로직 작성 예정)
│       │   └── resources/
│       └── test/
│           ├── java/com/ecosync/domain/
│           └── resources/
│
├── eco-sync-application/              # ② UseCase + Port 모듈 (라이브러리)
│   ├── build.gradle
│   └── src/
│       ├── main/
│       │   ├── java/com/ecosync/application/
│       │   │   └── (service, port 인터페이스 작성 예정)
│       │   └── resources/
│       └── test/
│           ├── java/com/ecosync/application/
│           └── resources/
│
├── eco-sync-infrastructure/           # ③ 기술 구현 모듈 (라이브러리)
│   ├── build.gradle
│   └── src/
│       ├── main/
│       │   ├── java/com/ecosync/infrastructure/
│       │   │   └── (JPA, 외부 API, 설정 등 작성 예정)
│       │   └── resources/
│       └── test/
│           ├── java/com/ecosync/infrastructure/
│           └── resources/
│
├── eco-sync-api/                      # ④ REST API 서버 (실행 모듈)
│   ├── build.gradle
│   ├── eco-sync-api.iml
│   └── src/
│       ├── main/
│       │   ├── java/com/ecosync/api/
│       │   │   ├── EcoSyncApiApplication.java   # Main 클래스 (포트 8080)
│       │   │   └── (controller, dto, config 작성 예정)
│       │   └── resources/
│       │       ├── application.yaml           # Base 설정 파일 (프로파일 정의)
│       │       ├── local/                     # 로컬 개발 환경 (기본값)
│       │       │   ├── server.yaml            # 포트 8080 설정
│       │       │   ├── database.yaml          # MySQL 로컬 연결 설정
│       │       │   └── logging.yaml           # 로깅 레벨 (DEBUG)
│       │       ├── dev/                       # 개발 서버 환경 (향후 추가)
│       │       │   ├── server.yaml
│       │       │   ├── database.yaml
│       │       │   └── logging.yaml
│       │       └── prod/                      # 프로덕션 환경 (향후 추가)
│       │           ├── server.yaml
│       │           ├── database.yaml
│       │           └── logging.yaml
│       └── test/
│           ├── java/com/ecosync/api/
│           │   └── EcoSyncApiApplicationTests.java
│           └── resources/
│
├── eco-sync-batch/                    # ⑤ 배치/스케줄링 서버 (실행 모듈)
│   ├── build.gradle
│   ├── eco-sync-batch.iml
│   └── src/
│       ├── main/
│       │   ├── java/com/ecosync/batch/
│       │   │   ├── EcoSyncBatchApplication.java # Main 클래스 (포트 8081)
│       │   │   └── (job, step, config 작성 예정)
│       │   └── resources/
│       │       ├── application.yaml           # Base 설정 파일 (프로파일 정의)
│       │       ├── local/                     # 로컬 개발 환경 (기본값)
│       │       │   ├── server.yaml            # 포트 8081 설정
│       │       │   ├── database.yaml          # MySQL 로컬 연결 설정
│       │       │   ├── batch.yaml             # Spring Batch 설정
│       │       │   └── logging.yaml           # 로깅 레벨 (DEBUG)
│       │       ├── dev/                       # 개발 서버 환경 (향후 추가)
│       │       │   ├── server.yaml
│       │       │   ├── database.yaml
│       │       │   ├── batch.yaml
│       │       │   └── logging.yaml
│       │       └── prod/                      # 프로덕션 환경 (향후 추가)
│       │           ├── server.yaml
│       │           ├── database.yaml
│       │           ├── batch.yaml
│       │           └── logging.yaml
│       └── test/
│           ├── java/com/ecosync/batch/
│           │   └── EcoSyncBatchApplicationTests.java
│           └── resources/
│
└── .claude/                           # Claude Code 세션 디렉토리
    └── (세션별 메모리 및 설정)
```

### 모듈별 주요 파일 목록

#### eco-sync-domain/ (순수 도메인 모듈)
```
주요 구성:
- src/main/java/com/ecosync/domain/
  └── (User, UserInterest, EconomicEvent 등 도메인 엔티티)
- src/test/java/com/ecosync/domain/
  └── (도메인 엔티티 단위 테스트)
```

#### eco-sync-application/ (UseCase & Port 모듈)
```
주요 구성:
- src/main/java/com/ecosync/application/
  ├── port/
  │   ├── in/          # Input Port (UseCase 인터페이스)
  │   └── out/         # Output Port (Repository, 외부 API 포트)
  └── service/         # UseCase 구현 (Service 클래스)
- src/test/java/com/ecosync/application/
  └── (Service 로직 단위/통합 테스트)
```

#### eco-sync-infrastructure/ (기술 구현 모듈)
```
주요 구성:
- src/main/java/com/ecosync/infrastructure/
  ├── persistence/     # JPA Entity, Repository 구현
  ├── external/        # 외부 API 호출 구현
  ├── config/          # DataSource, JPA, 매핑 설정
  └── mapper/          # MapStruct 매퍼 (Entity ↔ DTO)
- src/test/java/com/ecosync/infrastructure/
  └── (Repository, 외부 API 통합 테스트)
```

#### eco-sync-api/ (REST API 서버)
```
주요 구성:
- src/main/java/com/ecosync/api/
  ├── EcoSyncApiApplication.java  # Main 클래스 (포트 8080)
  ├── controller/                  # REST 컨트롤러 (@RestController)
  ├── dto/                         # Request/Response DTO
  └── config/                      # SecurityConfig, 예외 핸들러 등
- src/main/resources/
  ├── application.yaml             # Base 설정 파일 (프로파일 정의: local)
  ├── local/                       # 로컬 개발 환경 (기본값)
  │   ├── server.yaml              # server.port: 8080
  │   ├── database.yaml            # MySQL 로컬 연결 (ecosync_dev)
  │   └── logging.yaml             # logging.level: DEBUG
  ├── dev/                         # 개발 서버 환경 (향후 추가)
  │   ├── server.yaml
  │   ├── database.yaml
  │   └── logging.yaml
  └── prod/                        # 프로덕션 환경 (향후 추가)
      ├── server.yaml
      ├── database.yaml
      └── logging.yaml
- src/test/java/com/ecosync/api/
  └── EcoSyncApiApplicationTests.java  # 컨텍스트 로드 테스트
```

#### eco-sync-batch/ (배치 서버)
```
주요 구성:
- src/main/java/com/ecosync/batch/
  ├── EcoSyncBatchApplication.java # Main 클래스 (포트 8081)
  ├── job/                         # Spring Batch Job 설정
  ├── step/                        # Batch Step 구현
  └── config/                      # Quartz, Batch 설정
- src/main/resources/
  ├── application.yaml             # Base 설정 파일 (프로파일 정의: local)
  ├── local/                       # 로컬 개발 환경 (기본값)
  │   ├── server.yaml              # server.port: 8081
  │   ├── database.yaml            # MySQL 로컬 연결 (ecosync_dev)
  │   ├── batch.yaml               # Spring Batch, Quartz 설정
  │   └── logging.yaml             # logging.level: DEBUG
  ├── dev/                         # 개발 서버 환경 (향후 추가)
  │   ├── server.yaml
  │   ├── database.yaml
  │   ├── batch.yaml
  │   └── logging.yaml
  └── prod/                        # 프로덕션 환경 (향후 추가)
      ├── server.yaml
      ├── database.yaml
      ├── batch.yaml
      └── logging.yaml
- src/test/java/com/ecosync/batch/
  └── EcoSyncBatchApplicationTests.java  # 컨텍스트 로드 테스트
```

### 패키지 네이밍 규칙

| 모듈 | 기본 패키지 | 하위 패키지 예시 |
|------|------------|-----------------|
| **domain** | `com.ecosync.domain` | `entity`, `vo`, `exception` |
| **application** | `com.ecosync.application` | `port.in`, `port.out`, `service` |
| **infrastructure** | `com.ecosync.infrastructure` | `persistence`, `external`, `config`, `mapper` |
| **api** | `com.ecosync.api` | `controller`, `dto`, `config` |
| **batch** | `com.ecosync.batch` | `job`, `step`, `config` |

### 빌드 산출물

```
각 모듈의 빌드 결과:

eco-sync-domain/
└── build/libs/eco-sync-domain-0.0.1-SNAPSHOT.jar      # 라이브러리 JAR

eco-sync-application/
└── build/libs/eco-sync-application-0.0.1-SNAPSHOT.jar # 라이브러리 JAR

eco-sync-infrastructure/
└── build/libs/eco-sync-infrastructure-0.0.1-SNAPSHOT.jar  # 라이브러리 JAR

eco-sync-api/
└── build/libs/eco-sync-api-0.0.1-SNAPSHOT.jar         # 실행 가능한 Spring Boot JAR (bootJar)

eco-sync-batch/
└── build/libs/eco-sync-batch-0.0.1-SNAPSHOT.jar       # 실행 가능한 Spring Boot JAR (bootJar)
```

---

## 6. 개발 환경 설정

### 필수 요구사항

- **JDK 25** 이상 (Apple Silicon M2/M3 호환)
- **Gradle 9.3.1** (Gradle Wrapper로 자동 다운로드)
- **IDE:** IntelliJ IDEA 2024.x 이상 권장

### IDE 설정 (IntelliJ IDEA)

#### 프로젝트 임포트

1. **File** → **Open** → 프로젝트 루트 디렉토리 선택
2. IntelliJ가 `settings.gradle`을 감지하고 자동으로 Gradle 프로젝트 인식
3. **Trust Project** 클릭 (신뢰도 설정)
4. **File** → **Project Structure** 확인
   - **SDK:** JDK 25 설정
   - **Language Level:** 25
   - **Gradle:** Gradle 9.3.1

#### Gradle 설정

- **Settings** → **Build, Execution, Deployment** → **Gradle**
  - **Gradle JVM:** Project SDK (JDK 25)
  - **Build and run using:** Gradle
  - **Delegate IDE build/run actions to Gradle:** ✅ (체크)

#### 플러그인 설정

- Spring Boot 플러그인 설치 (자동)
- Gradle 플러그인 설치 (자동)
- Lombok 플러그인 설치
  - **Settings** → **Plugins** → "Lombok" 검색 후 설치

### 로컬 실행 준비

1. **프로젝트 임포트** 완료
2. **Gradle 빌드** 완료 (`./gradlew build`)
3. **IDE 캐시 새로고침** (File → Invalidate Caches)
4. **API/Batch 애플리케이션 실행**

---

## 7. 자주 사용하는 Gradle 명령어

### 빌드 관련 명령어

```bash
# 전체 프로젝트 빌드 (테스트 포함)
./gradlew build

# 전체 프로젝트 빌드 (테스트 제외)
./gradlew build -x test

# 특정 모듈 빌드
./gradlew :eco-sync-api:build
./gradlew :eco-sync-batch:build

# 빌드 결과 정리 (전체)
./gradlew clean
```

### 서버 실행 명령어

```bash
# API 서버 실행 (포트 8080)
./gradlew :eco-sync-api:bootRun

# Batch 서버 실행 (포트 8081)
./gradlew :eco-sync-batch:bootRun

# 특정 프로필로 실행
./gradlew :eco-sync-api:bootRun --args='--spring.profiles.active=dev'
./gradlew :eco-sync-batch:bootRun --args='--spring.profiles.active=prod'
```

### 테스트 관련 명령어

```bash
# 전체 테스트 실행
./gradlew test

# 특정 모듈 테스트만 실행
./gradlew :eco-sync-api:test
./gradlew :eco-sync-batch:test
./gradlew :eco-sync-domain:test

# 특정 테스트 클래스 실행
./gradlew :eco-sync-api:test --tests EcoSyncApiApplicationTests

# 특정 테스트 메서드 실행
./gradlew :eco-sync-api:test --tests EcoSyncApiApplicationTests.contextLoads
```

### 의존성 관련 명령어

```bash
# 모듈의 의존성 트리 확인
./gradlew :eco-sync-api:dependencies

# 런타임 의존성만 확인
./gradlew :eco-sync-api:dependencies --configuration runtimeClasspath

# 컴파일 의존성만 확인
./gradlew :eco-sync-api:dependencies --configuration compileClasspath

# 전체 프로젝트 의존성 그래프 생성
./gradlew buildEnvironment
```

### 기타 유용한 명령어

```bash
# Gradle 정보 확인
./gradlew --version

# 사용 가능한 태스크 목록 확인
./gradlew tasks

# 증분 빌드로 빌드 속도 확인
./gradlew build --info

# 병렬 빌드 (속도 향상)
./gradlew build --parallel

# 캐시 비우고 빌드 (완전한 재빌드)
./gradlew clean build
```

---

## 8. 테스트 실행 방법

### 테스트 구조

- **Unit Test:** `src/test/java/`
- **Integration Test:** `@SpringBootTest` 또는 `@DataJpaTest` 사용
- **테스트 리소스:** `src/test/resources/`

### 테스트 유형별 실행

```bash
# ① Domain 모듈 테스트 (순수 JUnit, Spring 없음)
./gradlew :eco-sync-domain:test

# ② Application 모듈 테스트 (Mock Port 주입)
./gradlew :eco-sync-application:test

# ③ Infrastructure 모듈 테스트 (@DataJpaTest 등)
./gradlew :eco-sync-infrastructure:test

# ④ API 모듈 테스트 (@WebMvcTest 등)
./gradlew :eco-sync-api:test

# ⑤ Batch 모듈 테스트 (@SpringBatchTest 등)
./gradlew :eco-sync-batch:test
```

### IDE에서 테스트 실행

#### IntelliJ IDEA

1. **테스트 클래스 오픈** (`EcoSyncApiApplicationTests.java` 등)
2. **클래스 이름 좌측 녹색 화살표** 클릭 → 전체 클래스 테스트
3. **메서드 좌측 녹색 화살표** 클릭 → 특정 메서드만 테스트
4. **Run** → **Run 'className'** (Ctrl+Shift+F10)

### 테스트 커버리지 확인

```bash
# JaCoCo로 커버리지 리포트 생성
./gradlew test jacocoTestReport

# 리포트 위치: `build/reports/jacoco/test/html/index.html`
```

### CI/CD에서 테스트 건너뛰기

```bash
# 테스트 없이 빌드만 수행 (배포 전 최종 검증은 CI/CD에서)
./gradlew build -x test
```

---

## 9. 설정 파일 구조

### 개요

Spring Boot 설정 파일은 **프로파일별로 분리된 폴더 구조**를 사용합니다. `application.yaml`에서 `${spring.profiles.active}` 변수를 사용하여 프로파일에 맞는 폴더의 설정 파일들을 동적으로 로드합니다.

- **application.yaml**: Base 설정 (프로파일 정의, config import)
- **local/**: 로컬 개발 환경 (기본값, Docker MySQL)
- **dev/**: 개발 서버 환경 (향후 추가)
- **prod/**: 프로덕션 환경 (향후 추가)

### API 모듈 설정 구조

```
eco-sync-api/src/main/resources/
├── application.yaml              # Base 설정 파일 (프로파일 정의)
├── local/                        # 로컬 개발 환경 (기본값)
│   ├── server.yaml              # server.port: 8080
│   ├── database.yaml            # MySQL 로컬 연결 (ecosync_dev)
│   └── logging.yaml             # logging.level: DEBUG
├── dev/                          # 개발 서버 환경 (향후 추가)
│   ├── server.yaml
│   ├── database.yaml
│   └── logging.yaml
└── prod/                         # 프로덕션 환경 (향후 추가)
    ├── server.yaml
    ├── database.yaml
    └── logging.yaml
```

**application.yaml:**
```yaml
spring:
  profiles:
    active: local  # 기본 프로파일 (local, dev, prod 중 선택)
  config:
    import:
      - classpath:${spring.profiles.active}/server.yaml
      - classpath:${spring.profiles.active}/database.yaml
      - classpath:${spring.profiles.active}/logging.yaml
```

> `${spring.profiles.active}` 변수는 프로파일 값으로 자동 치환됩니다.
> 예: `local`이면 `classpath:local/server.yaml` 로드, `dev`이면 `classpath:dev/server.yaml` 로드

### Batch 모듈 설정 구조

API 모듈과 동일한 구조이며, `local/` 폴더에 `batch.yaml`이 추가됩니다:

```
eco-sync-batch/src/main/resources/
├── application.yaml
├── local/
│   ├── server.yaml              # server.port: 8081
│   ├── database.yaml
│   ├── batch.yaml               # Spring Batch, Quartz 설정
│   └── logging.yaml
├── dev/
│   ├── server.yaml
│   ├── database.yaml
│   ├── batch.yaml
│   └── logging.yaml
└── prod/
    ├── server.yaml
    ├── database.yaml
    ├── batch.yaml
    └── logging.yaml
```

### 프로파일 활성화

```bash
# local 프로필 (기본값, 명시 불필요)
./gradlew :eco-sync-api:bootRun
./gradlew :eco-sync-batch:bootRun

# dev 프로필로 실행
./gradlew :eco-sync-api:bootRun --args='--spring.profiles.active=dev'
./gradlew :eco-sync-batch:bootRun --args='--spring.profiles.active=dev'

# prod 프로필로 실행
./gradlew :eco-sync-api:bootRun --args='--spring.profiles.active=prod'
./gradlew :eco-sync-batch:bootRun --args='--spring.profiles.active=prod'

# JAR로 프로파일 지정
java -jar eco-sync-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
java -jar eco-sync-batch-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 로컬 개발 환경 세팅

로컬 MySQL 환경은 Docker Compose로 구성합니다.

**1. Docker MySQL 시작**
```bash
cd infra/local
cp .env .env.backup  # 기존 설정 백업 (선택)
docker-compose up -d
```

**2. 데이터베이스 생성 및 초기 데이터 로드**
Docker Compose 시작 시 자동으로 생성됩니다:
- MySQL 포트: `3306`
- PhpMyAdmin: `http://localhost:8888`
- 데이터베이스: `ecosync_dev`
- 사용자: `ecosync_user` / 비밀번호: `ecosync_password_dev`

**3. 애플리케이션 실행 (local 프로파일 사용)**
```bash
# API 서버 (기본값: local 프로파일, 로컬 MySQL 자동 연결)
./gradlew :eco-sync-api:bootRun

# Batch 서버 (기본값: local 프로파일, 로컬 MySQL 자동 연결)
./gradlew :eco-sync-batch:bootRun
```

**설정 파일 위치:**
- API 설정: `eco-sync-api/src/main/resources/local/`
- Batch 설정: `eco-sync-batch/src/main/resources/local/`

**다른 프로파일 사용:**
```bash
# 개발 서버 환경 (dev 프로파일)
./gradlew :eco-sync-api:bootRun --args='--spring.profiles.active=dev'

# 프로덕션 환경 (prod 프로파일)
./gradlew :eco-sync-api:bootRun --args='--spring.profiles.active=prod'
```

자세한 로컬 개발 가이드는 `infra/local/README.md`를 참고하세요.

---

## 10. 주요 의존성 및 버전 관리

### 버전 관리 방식

모든 의존성 버전은 **gradle/libs.versions.toml** (Version Catalog)에서 중앙 관리됩니다.

```toml
[versions]
spring-boot = "4.0.3"
spring-dep-mgmt = "1.1.7"
java = "25"
ical4j = "3.2.19"
mapstruct = "1.5.5.Final"

[libraries]
spring-boot-starter-web = { group = "org.springframework.boot", name = "spring-boot-starter-web", version.ref = "spring-boot" }
# ... 기타 라이브러리
```

### 핵심 의존성

| 라이브러리 | 버전 | 모듈 | 용도 |
|-----------|------|------|------|
| **Spring Boot BOM** | 4.0.3 | 모든 모듈 | Spring 생태계 버전 관리 |
| **Spring Data JPA** | 4.0.3 | infrastructure, api, batch | ORM 데이터 접근 |
| **Hibernate** | 6.4.x | infrastructure | JPA 구현체 |
| **H2 Database** | 2.1.214 | infrastructure | 개발/테스트용 인메모리 DB |
| **MySQL Connector** | 8.2.0 | infrastructure | 프로덕션 데이터베이스 |
| **MapStruct** | 1.5.5.Final | infrastructure | Entity-DTO 매핑 |
| **ical4j** | 3.2.19 | infrastructure | iCal 파일 생성 |
| **Spring Batch** | 5.0.x | batch | 대용량 배치 처리 |
| **Quartz Scheduler** | 2.3.x | batch | 정기 작업 스케줄링 |
| **Spring Security** | 6.2.x | api | 인증/인가 |
| **Lombok** | 1.18.x | 모든 모듈 | 보일러플레이트 제거 |
| **JUnit 5** | 5.x | 모든 모듈 | 테스트 프레임워크 |

### 의존성 추가 방법

1. **gradle/libs.versions.toml**에 버전 추가:
   ```toml
   [versions]
   new-library = "1.0.0"

   [libraries]
   my-library = { group = "com.example", name = "my-library", version.ref = "new-library" }
   ```

2. **모듈의 build.gradle**에서 사용:
   ```gradle
   dependencies {
       implementation libs.myLibrary
   }
   ```

---

## 11. 주요 기술 적용 패턴

### JPA/Hibernate 패턴

#### Entity 설계

```java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 36)
    private String calendarToken;

    @Column(nullable = true)
    private String createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private String updatedBy;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = true)
    private LocalDateTime deletedAt;
}
```

#### Repository 패턴

```java
// Infrastructure 모듈의 Output Port 구현
@Repository
public class UserJpaAdapter implements UserPort {

    private final UserJpaRepository repository;

    @Override
    public User save(User user) {
        return repository.save(user);
    }
}

// Spring Data JPA Interface
@Repository
interface UserJpaRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByCalendarToken(String calendarToken);
}
```

### MapStruct 매핑 패턴

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDto toDto(User user);
    User toDomain(UserRequestDto dto);

    List<UserResponseDto> toDtoList(List<User> users);
}

// 사용
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper mapper;
    private final UserPort port;

    public UserResponseDto getUser(Long id) {
        User user = port.findById(id);
        return mapper.toDto(user);
    }
}
```

### ical4j 캘린더 처리

```java
@Service
@RequiredArgsConstructor
public class IcsGeneratorService {

    public String generateIcs(List<EconomicEvent> events) {
        Calendar calendar = new Calendar();
        calendar.add(new ProdId("-//EcoSync//NONSGML v1.0//EN"));
        calendar.add(Version.VERSION_2_0);
        calendar.add(CalScale.GREGORIAN);

        for (EconomicEvent event : events) {
            VEvent vevent = new VEvent();
            vevent.add(new Summary(event.getTitle()));
            vevent.add(new DtStart(new DateTime(event.getEventDatetime())));
            vevent.add(new Description(event.getDescription()));
            calendar.add(vevent);
        }

        return calendar.toString();
    }
}
```

### Spring Batch 패턴

```java
@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Bean
    public Job exampleJob(JobRepository jobRepository, PlatformTransactionManager tm) {
        return new JobBuilder("exampleJob", jobRepository)
                .start(exampleStep(jobRepository, tm))
                .build();
    }

    @Bean
    public Step exampleStep(JobRepository jobRepository, PlatformTransactionManager tm) {
        return new StepBuilder("exampleStep", jobRepository)
                .<Input, Output> chunk(100, tm)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }
}
```

### Quartz 스케줄링

```java
@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail exampleJobDetail() {
        return JobBuilder.newJob(ExampleJob.class)
                .withIdentity("exampleJob")
                .build();
    }

    @Bean
    public Trigger exampleTrigger(JobDetail exampleJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(exampleJobDetail)
                .withIdentity("exampleTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 * * * ?")) // 매 시간
                .build();
    }
}
```

### Spring Security 인증/인가

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
```

### Lombok 사용 규칙

```java
// Entity나 DTO에는 @Data 사용
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User {
    @Id
    private Long id;
    private String email;
    private String calendarToken;
}

// Service에는 @RequiredArgsConstructor 사용 (생성자 주입)
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserPort port;

    public User save(User user) {
        return port.save(user);
    }
}
```

---

## 12. 배포 관련 설정

### 빌드 아티팩트

- **라이브러리 모듈** (domain, application, infrastructure)
  - 생성물: `build/libs/*.jar` (일반 JAR)
  - 용도: 다른 프로젝트에서 재사용

- **실행 모듈** (api, batch)
  - 생성물: `build/libs/*-SNAPSHOT.jar` (bootJar)
  - 용도: 독립 실행 가능한 Spring Boot 애플리케이션

### 모듈별 빌드

```bash
# API 서버 빌드
./gradlew :eco-sync-api:build

# 생성 위치: eco-sync-api/build/libs/eco-sync-api-0.0.1-SNAPSHOT.jar

# Batch 서버 빌드
./gradlew :eco-sync-batch:build

# 생성 위치: eco-sync-batch/build/libs/eco-sync-batch-0.0.1-SNAPSHOT.jar
```

### 프로필별 빌드 및 실행

```bash
# 로컬 개발 환경에서의 빌드 및 실행 (Docker MySQL)
# 먼저 infra/local/docker-compose up -d 실행 필수
./gradlew :eco-sync-api:build
java -jar eco-sync-api/build/libs/eco-sync-api-0.0.1-SNAPSHOT.jar \
    --spring.profiles.active=dev

# 프로덕션 환경에서의 빌드 (원격 MySQL)
./gradlew :eco-sync-api:build
java -jar eco-sync-api/build/libs/eco-sync-api-0.0.1-SNAPSHOT.jar \
    --spring.profiles.active=prod \
    --spring.datasource.url=jdbc:mysql://prod-db-host:3306/ecosync \
    --spring.datasource.username=prod_user \
    --spring.datasource.password=prod_password
```

### 서버별 포트 설정

| 서버 | 포트 | 설정 파일 | 설명 |
|------|------|---------|------|
| API | 8080 | `config/local/server.yaml` | REST API 서버 |
| Batch | 8081 | `config/local/server.yaml` | 배치 처리 서버 |
| MySQL | 3306 | `infra/local/docker-compose.yaml` | 로컬 개발 데이터베이스 |
| PhpMyAdmin | 8888 | `infra/local/docker-compose.yaml` | MySQL 웹 관리 도구 |

### 로컬 MySQL 개발 환경 세팅

**1. Docker MySQL 시작:**
```bash
cd infra/local
cp .env.example .env
docker-compose up -d
```

**2. 로컬 설정 파일:**
```
config/local/
├── server.yaml              # 포트 설정
├── database-dev.yaml        # MySQL 연결 (로컬)
├── batch.yaml              # Spring Batch 설정 (Batch만)
└── logging.yaml            # 로깅 레벨
```

**3. 로컬에서 실행:**
```bash
# API 실행 (dev 프로필 자동 사용)
./gradlew :eco-sync-api:bootRun --args='--spring.profiles.active=dev'

# Batch 실행 (dev 프로필 자동 사용)
./gradlew :eco-sync-batch:bootRun --args='--spring.profiles.active=dev'
```

## 13. 개발 팁 및 주의사항

### Domain 레이어 순수성 유지

**❌ 금지된 패턴:**
```java
// Domain 엔티티가 Spring/JPA 사용 - 금지!
@Entity
public class Country {
    @Column
    private String name;
}
```

**✅ 올바른 패턴:**
```java
// 순수 Java POJO - 권장!
public class Country {
    private String name;

    // 기본 생성자, Getter/Setter만 사용
}
```

### Output Port 설계 패턴

**❌ 잘못된 예:**
```java
// Application이 JPA Repository를 직접 참조 - 금지!
@Service
public class CountryService {
    @Autowired
    private CountryJpaRepository repository;  // 직접 의존
}
```

**✅ 올바른 예:**
```java
// Application이 Port 인터페이스에만 의존
@Service
public class CountryService {
    private final CountryPort port;

    public CountryService(CountryPort port) {
        this.port = port;  // 인터페이스 의존
    }
}

// Infrastructure에서 Port 구현
@Repository
public class CountryAdapter implements CountryPort {
    private final CountryJpaRepository repository;

    @Override
    public Country save(Country country) {
        return repository.save(country);
    }
}
```

### 순환 의존성 방지

**❌ 금지된 패턴:**
```
api → application
      ↑
      └─ infrastructure (금지!)

application → infrastructure
              ↑
              └─ application (순환!)
```

**✅ 올바른 패턴:**
```
api, batch → application → domain
            ↓
          infrastructure (Port 구현)
                        ↓
                      domain
```

---

## 14. Git 컨벤션

### 브랜치 전략

```
main (프로덕션)
  ↑
  └── dev (개발 통합 브랜치)
        ↑
        ├── feature/TSK-* (기능 개발)
        ├── bugfix/TSK-* (버그 수정)
        ├── chore/TSK-* (유지보수)
        └── docs/TSK-* (문서화)
```

**브랜치 명명 규칙:**
- `feature/TSK-123-description`: 새 기능 (예: `feature/TSK-8-db-schema-setup`)
- `bugfix/TSK-123-description`: 버그 수정 (예: `bugfix/TSK-15-charset-issue`)
- `chore/TSK-123-description`: 리팩토링, 의존성 업데이트 등 (예: `chore/TSK-20-update-gradle`)
- `docs/TSK-123-description`: 문서화 (예: `docs/TSK-5-add-api-guide`)

### 커밋 메시지 형식

**Conventional Commits 기반:**

```
[TYPE-TICKET] 제목

본문 (선택사항)

Footer (선택사항)
```

**TYPE 종류:**
- `feat`: 새로운 기능
- `fix`: 버그 수정
- `docs`: 문서화
- `style`: 코드 스타일 변경 (포맷팅, 세미콜론 등)
- `refactor`: 코드 리팩토링
- `perf`: 성능 최적화
- `test`: 테스트 추가/수정
- `chore`: 빌드, 의존성, 도구 설정 변경
- `infra`: 인프라/배포 관련

**예시:**

```
[feat-TSK-8] 데이터베이스 스키마 설정

eco-sync-backend 초기 설정:
- Docker MySQL 환경 구성
- 3개 테이블 생성 (users, user_interests, economic_events)
- UTF-8MB4 charset 설정

Fixes #8
```

```
[fix-TSK-15] MySQL 한글 데이터 깨짐 현상 해결

my.cnf 설정 파일 추가로 전역 charset 설정
- 기존 임시 SQL 설정 제거
- Docker Compose에 my.cnf 마운트 설정

Fixes #15
```

```
[refactor-TSK-10] DDL에서 FK 제약 제거

성능 최적화를 위해 user_interests → users FK 제거
관계는 애플리케이션 레벨에서 관리

Relates to #10
```

### Pull Request (PR) 템플릿

```markdown
## 📝 설명
[변경사항에 대한 간단한 설명]

## 🎯 이슈
Fixes #[ISSUE_NUMBER]

## 📋 변경 사항
- [ ] 기능 추가
- [ ] 버그 수정
- [ ] 문서 수정
- [ ] 기타: ___________

## 🔍 상세 내용
[자세한 설명, 왜 이런 변경이 필요했는지, 어떤 접근 방식을 선택했는지]

## ✅ 테스트
- [ ] 로컬 환경에서 테스트 완료
- [ ] 관련 테스트 추가/수정

## 📸 스크린샷 (필요시)
[스크린샷 또는 데모 URL]

## 🚀 배포 시 주의사항
[배포 시 특별히 고려해야 할 사항]
```

### Squash Merge 정책

**main 브랜치 머지:**
```bash
git checkout main
git pull origin main
git merge --squash feature/TSK-123-description
git commit -m "[feat-TSK-123] 기능 설명

상세한 설명을 여기 작성합니다.

Fixes #123"
git push origin main
```

**Squash 커밋 메시지:**
- 전체 변경사항을 한 줄로 요약
- 본문에 주요 변경 내용 기술
- Issue 번호 참고 (Fixes #123 또는 Relates to #123)

### Git 커밋 체크리스트

PR/커밋 전 확인사항:
- [ ] 커밋 메시지가 Conventional Commits 형식인가?
- [ ] 이슈 번호가 포함되어 있는가?
- [ ] 한 커밋에 한 가지 변경사항만 포함되어 있는가?
- [ ] 테스트를 실행했는가?
- [ ] 불필요한 파일(`.DS_Store`, `*.class`, `.env`)을 커밋하지 않았는가?
- [ ] 커밋 메시지에 한글이 올바르게 인코딩되었는가?

### 자동화된 커밋 메시지 생성 (선택)

Claude Code에서 커밋 시 자동 생성되는 메시지 형식:
```
[TYPE-TSK-NUMBER] 간단한 설명

상세 내용

Co-Authored-By: Claude [Model] <noreply@anthropic.com>
```

### 커밋 그룹핑 규칙 (필수)

**비슷한 변경사항끼리 함께 커밋하세요.**

#### 1️⃣ 데이터베이스/인프라 커밋
**함께 커밋할 파일:**
- `infra/local/sql/*.sql` (DDL, 초기 데이터)
- `infra/local/docker-compose.yaml`
- `infra/local/my.cnf`

**예시:**
```
[infra-TSK-2] 데이터베이스 스키마 및 환경 설정

- DDL: 3개 테이블 생성 (users, user_interests, economic_events)
- 메타필드: created_by, created_at, updated_by, updated_at, deleted_at 추가
- my.cnf: UTF-8MB4 charset 설정
- docker-compose: my.cnf 마운트 추가
- 초기 데이터: 샘플 사용자 및 경제 일정 로드

Closes #2
```

#### 2️⃣ 문서/가이드 커밋
**함께 커밋할 파일:**
- `CLAUDE.md` (개발 가이드)
- `README.md`
- `docs/**`

**예시:**
```
[docs-TSK-5] Git 컨벤션 및 커밋 가이드라인 작성

- 브랜치 전략 및 명명 규칙 정의
- Conventional Commits 형식 가이드
- PR 템플릿 및 커밋 체크리스트 추가
- 커밋 그룹핑 규칙 정의

Closes #5
```

#### 3️⃣ 기능 구현 커밋
**함께 커밋할 파일:**
- `src/main/java/**` (기능 코드)
- `src/test/java/**` (관련 테스트)

**원칙:**
> 한 커밋에는 **한 가지 기능**만 포함하세요.

#### ❌ 피해야 할 패턴

```
❌ 여러 주제를 한 커밋에 포함
[feat-TSK-20] 사용자 API + Docker 설정 + 문서화

✅ 주제별로 분리
[feat-TSK-20] 사용자 조회 API 구현
[infra-TSK-21] Docker MySQL 환경 설정
[docs-TSK-22] API 사용 가이드 문서화
```

---

## 15. 추가 참고 자료 및 링크

### 프로젝트 관련 문서

- **ADR-001**: EcoSync Backend 아키텍처 결정 기록 (Notion)
- **프로젝트 저장소**: https://github.com/eco-auto-sync/backend

### 기술 문서

- [Spring Boot 4.0.3 공식 문서](https://spring.io/projects/spring-boot)
- [Spring Data JPA 공식 문서](https://spring.io/projects/spring-data-jpa)
- [Gradle 공식 문서](https://docs.gradle.org/)
- [MapStruct 공식 문서](https://mapstruct.org/)
- [Spring Batch 공식 문서](https://spring.io/projects/spring-batch)
- [Quartz Scheduler 공식 문서](http://www.quartz-scheduler.org/)

### Hexagonal Architecture 참고 자료

- [육각형 아키텍처 설명 (알리스타 코크번)](https://alistair.cockburn.us/hexagonal-architecture/)
- [Ports & Adapters 패턴](https://en.wikipedia.org/wiki/Hexagonal_architecture)

### 개발 도구 및 설정

- **IntelliJ IDEA:** https://www.jetbrains.com/idea/
- **Gradle Wrapper:** `./gradlew`로 자동 버전 관리
- **Java 25 설치 가이드:** https://www.oracle.com/java/technologies/downloads/

### 자주 묻는 질문 (FAQ)

**Q: Domain 모듈에 Spring 의존성을 추가해야 하는 경우?**
A: Domain은 순수해야 합니다. 필요하면 Application이나 Infrastructure에 로직을 옮기세요.

**Q: 모듈 간 엔티티 공유?**
A: 가능하면 피하세요. DTO를 통해 계층 간 통신하세요.

**Q: 새로운 의존성 추가 방법?**
A: `gradle/libs.versions.toml`에 버전 추가 후 `build.gradle`에서 참조하세요.

**Q: 배치 작업이 DB를 잠금?**
A: JPA 배치 설정(`batch_size`, `fetch_size`)을 조정하세요.

---

**마지막 업데이트:** 2026년 3월 15일
**프로젝트 상태:** Hexagonal Architecture 기본 구조 완성 + 로컬 MySQL 개발 환경 구성
**현재 단계:**
- ✅ 멀티모듈 구조 설계 완료
- ✅ 인프라 설정 (Docker Compose + MySQL + PhpMyAdmin)
- ✅ 환경별 설정 파일 구조화 (config/local/ 분리)
- ✅ API/Batch 모듈 MySQL 연결
- ⏳ 도메인 엔티티 및 API 구현 시작
