# QRon

**안드로이드 QR 스캐너** — 카메라 스캔과 퀵 설정 타일을 통한 화면 내 스캔을 지원합니다.

모든 인식은 온디바이스에서 처리되어 네트워크 전송이 없습니다.

QRon은 두 가지 방식으로 QR을 인식합니다.

- 일반 카메라 스캔
- 상단바 퀵 설정 타일을 눌러 **현재 화면에 떠 있는 QR을 그대로 캡처·인식**하는
  화면 내 스캔입니다.

---

## 주요 기능

- **카메라 스캔**: CameraX 프리뷰 실시간 인식, 핀치 줌 지원.
- **화면 내 스캔**: 퀵 설정 타일 → 접근성 서비스 화면 캡처 → QR 인식. 다른 앱 화면의 QR도 인식.
- **스캔 기록**: Room 기반 영속화, 출처(카메라/화면) 필터, 전체 삭제(확인 다이얼로그).
- **결과 화면**: 인식된 값과 QR 크롭 이미지를 표시.
- **URL / WiFi / 연락처 / 텍스트** 등 QR 타입별 파싱.

---

## 화면 내 스캔 사용법

1. 앱 실행 후 **접근성 설정**에서 `QRon`의 접근성 서비스를 활성화합니다. (화면 캡처 권한)
2. 상단 퀵 설정 패널에 **QR 화면 스캔** 타일을 추가합니다.
3. QR이 표시된 화면에서 타일을 누르면 현재 화면을 캡처해 QR을 인식합니다.

---

## 아키텍처

구글 공식 레퍼런스인 **Now in Android(NiA)의 다중 모듈 클린 아키텍처**를 지향합니다. 의존성 방향은 `Feature → Domain ← Data`이며,
`feature` 모듈은 `data`에 직접 의존하지 않고 `domain`과 `core:designsystem`만 참조합니다. `domain`은 안드로이드 의존성이 없는 순수
코틀린 모듈입니다.

```
app
├─ domain
├─ data ─────────────► domain, core:database
├─ core:designsystem
├─ feature:history ──► domain, core:designsystem
├─ feature:scanner ──► domain, core:designsystem, core:vision, feature:result
├─ feature:result ──► domain, core:designsystem
└─ feature:scan ────► domain, core:vision

core:database ──► domain (Room + KSP)
core:vision ───► domain (ML Kit Barcode)
domain ────────► 순수 코틀린 (coroutines-core 만 의존)
```

UI는 전적으로 **Jetpack Compose**, 상태 관리는 **MVVM + 단방향 데이터 흐름(UDF)** 을 따릅니다. ViewModel은 화면 상태를 단일
`StateFlow`로 노출하고, 비동기는 Kotlin Coroutines/Flow를 사용합니다.

### 모듈 구성

| 레이어     | 모듈                  | 역할                                                     |
|---------|---------------------|--------------------------------------------------------|
| App     | `app`               | 진입점, 단일 `NavHost` 네비게이션, Hilt 루트                       |
| Domain  | `domain`            | 순수 코틀린 비즈니스 계층 (Entity, Repository Interface, Usecase) |
| Data    | `data`              | 도메인 인터페이스 구현 + Hilt 바인딩                                |
| Core    | `core:database`     | Room DB, DAO, Entity                                   |
| Core    | `core:vision`       | ML Kit Barcode Scanning 데이터소스                          |
| Core    | `core:designsystem` | Compose 테마(색·타이포)                                      |
| Feature | `feature:scanner`   | CameraX 실시간 스캔 화면                                      |
| Feature | `feature:scan`      | 화면 내 스캔 (퀵 타일 + 접근성 서비스)                               |
| Feature | `feature:result`    | 스캔 결과 화면                                               |
| Feature | `feature:history`   | 스캔 기록 목록·필터 화면                                         |

---

## 기술 스택

| 분류   | 라이브러리 / 도구              | 버전     |
|------|-------------------------|--------|
| 빌드   | Android Gradle Plugin   | 9.2.1  |
| 언어   | Kotlin (JVM Target 21)  | 2.3.21 |
| 코드생성 | KSP                     | 2.3.5  |
| DI   | Hilt                    | 2.59.2 |
| UI   | Jetpack Compose         | BOM 기반 |
| DB   | Room                    | 2.6.1  |
| 카메라  | CameraX                 | 1.6.1  |
| 인식   | ML Kit Barcode Scanning | 17.3.0 |
| 비동기  | Kotlinx Coroutines      | 1.8.1  |

- **SDK**: `compileSdk 37` · `minSdk 26` · `targetSdk 35`
- 모든 의존성은 **Version Catalog(`gradle/libs.versions.toml`)** 에서 중앙 관리합니다. `build.gradle`에 버전을 하드코딩하지
  않습니다.
- 공통 빌드 설정은 `build-logic`의 **Convention Plugin**(`my.android.application`, `my.android.library`,
  `my.android.compose`, `my.android.hilt`, `my.android.feature`)으로 추출되어 있습니다. SDK 정책은 개별 모듈이 아니라 컨벤션
  플러그인에서 변경합니다.

---