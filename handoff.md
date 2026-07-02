# QRon 프로젝트 핸드오프 문서 (Handoff)

> 작성일: 2026-07-02 · 대상 브랜치: `develop` (HEAD `a5f28e6`) · 작성 기준: 로컬 워킹 트리 스냅샷

이 문서는 QRon 프로젝트를 처음 이어받는 개발자(또는 협업 에이전트)가 **현재 상태를 빠르게 파악하고 다음 작업에 바로 착수**할 수 있도록 정리한 인수인계 문서입니다. 아키텍처 원칙과 협업 규칙의 상세 내용은 저장소 루트의 `AGENTS.md`를 함께 참고하세요.

---

## 1. 프로젝트 개요

QRon은 안드로이드용 **온디바이스 QR 스캐너** 앱입니다. 두 가지 스캔 방식을 지향합니다.

- **외부 스캔 (External / Camera):** CameraX 프리뷰 위에서 ML Kit로 QR을 실시간 인식하는 일반적인 카메라 스캔.
- **화면 내 스캔 (In-screen / Quick Settings Tile):** 상단바 퀵 설정 타일을 눌러 현재 화면에 표시된 QR을 인식하는 방식. 접근성 서비스(AccessibilityService)와 타일 서비스(TileService)를 활용합니다.

패키지 네임스페이스는 `com.peanutbutter1001.qron`, 앱 버전은 `versionCode 1 / versionName 1.0`(출시 전)입니다.

핵심 설계 철학은 구글 공식 레퍼런스인 **Now in Android(NiA)의 다중 모듈 클린 아키텍처**를 최우선으로 따르는 것입니다. UI는 전적으로 Jetpack Compose, 상태 관리는 MVVM + 단방향 데이터 흐름(UDF, `StateFlow`), 비동기는 Kotlin Coroutines/Flow를 사용합니다.

---

## 2. 현재 상태 한눈에 보기

| 항목 | 상태 |
|---|---|
| 기본 브랜치 | `develop` (원격 `origin/develop`과 fast-forward 동기화됨) |
| 최신 커밋 | `refactor(build): 모듈별 중복 빌드 의존성 통합 및 불필요한 리소스 제거` |
| 아키텍처 | ✅ NiA 기반 다중 모듈화 + 클린 아키텍처 전면 적용 완료 |
| 빌드 규약(build-logic) | ✅ Convention Plugin 5종 구축 완료 |
| MVP QR 스캔/기록 | ✅ 최초 구현 완료 (이후 모듈 분리 리팩터링 반영) |
| 앱 진입/네비게이션 | 🟡 하단 탭(스캐너·기록) 셸만 구성, 일부 콜백 미구현(TODO) |
| 화면 내 스캔(타일+접근성) | 🟡 서비스/매니페스트 등록 완료, 실동작 통합 검증 필요 |
| CI/CD | ✅ GitHub Actions PR 체크 + Release AAB 빌드 구성 |
| 테스트 | 🔴 실질적 단위/UI 테스트 미작성 (테스트 의존성만 정의) |

범례: ✅ 완료 · 🟡 진행 중/부분 완료 · 🔴 미착수

---

## 3. 아키텍처 & 모듈 구조

프로젝트는 `app` + `domain` + `data` + `core/*` + `feature/*`의 다중 모듈로 구성되며, 의존성 방향은 **Feature → Domain ← Data**의 클린 아키텍처 원칙을 따릅니다. 특히 `feature` 모듈들은 `data` 모듈에 직접 의존하지 않도록 분리(decouple)되어 있고, 오직 `domain`과 `core:designsystem`만 참조합니다.

### 모듈 의존성 그래프

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
domain ────────► (순수 코틀린, coroutines-core 만 의존)
```

### 모듈별 역할 및 구현 현황

| 레이어 | 모듈 | 역할 | 주요 파일 | 상태 |
|---|---|---|---|---|
| App | `app` | 진입점, 하단 탭 네비게이션, Hilt 루트 | `MainActivity.kt`, `QRonApplication.kt` | 🟡 탭 셸 완성, 콜백 TODO 존재 |
| Domain | `domain` | 순수 코틀린 비즈니스 계층 | `model/QRResult.kt`, `repository/HistoryRepository.kt` | ✅ 엔티티·리포지토리 인터페이스 정의 |
| Data | `data` | 도메인 인터페이스 구현 + DI 바인딩 | `repository/HistoryRepositoryImpl.kt`, `mapper/HistoryMapper.kt`, `di/DataModule.kt` | ✅ Room 기반 기록 저장 구현 |
| Core | `core:database` | Room DB, DAO, 엔티티 | `AppDatabase.kt`, `HistoryDao.kt`, `HistoryEntity.kt`, `Converters.kt`, `di/DatabaseModule.kt` | ✅ 기록 영속화 구성 |
| Core | `core:vision` | ML Kit 바코드 인식 데이터소스 | `QRScannerDataSource.kt` | ✅ 인식 로직 캡슐화 |
| Core | `core:designsystem` | Compose 테마(색·타이포) | `theme/Theme.kt`, `Color.kt`, `Type.kt` | ✅ 공통 테마 제공 |
| Feature | `feature:scanner` | CameraX 프리뷰 실시간 스캔 화면 | `ScannerScreen.kt`, `ScannerViewModel.kt` | 🟡 화면 구현, 결과 연동 검증 필요 |
| Feature | `feature:result` | 스캔 결과 표시 Activity | `ScanResultActivity.kt`, `ScanResultViewModel.kt` | 🟡 별도 Activity로 구현 |
| Feature | `feature:history` | 스캔 기록 목록 화면 | `HistoryScreen.kt`, `HistoryViewModel.kt` | 🟡 목록/초기화 구현, 필터 TODO |
| Feature | `feature:scan` | 화면 내 스캔 (타일 + 접근성 서비스) | `QRonTileService.kt`, `QRonAccessibilityService.kt` | 🟡 서비스 등록 완료, 동작 통합 필요 |

> 참고: `feature:scanner`는 스캔 결과 화면으로 이동하기 위해 `feature:result`에 의존합니다. 이는 NiA의 순수한 feature 간 무의존 원칙과는 다른 예외적 결합이므로, 향후 네비게이션 계층 도입 시 재검토 대상입니다.

---

## 4. 기술 스택

| 분류 | 라이브러리 / 도구 | 버전 |
|---|---|---|
| 빌드 | Android Gradle Plugin (AGP) | 9.2.1 |
| 언어 | Kotlin | 2.3.21 (JVM Target 21) |
| DI | Hilt | 2.59.2 (+ hilt-navigation-compose 1.3.0) |
| 코드생성 | KSP | 2.3.5 |
| UI | Jetpack Compose (BOM) | 2026.05.01 |
| DB | Room | 2.6.1 |
| 카메라 | CameraX | 1.3.4 |
| 인식 | ML Kit Barcode Scanning | 17.3.0 |
| 비동기 | Kotlinx Coroutines | 1.8.1 |

모든 의존성은 **Version Catalog(`gradle/libs.versions.toml`)** 에서 중앙 관리됩니다. `build.gradle.kts`에 버전을 하드코딩하지 말고 반드시 카탈로그에 먼저 정의한 뒤 참조해야 합니다.

### 빌드 규약 (build-logic / Convention Plugins)

NiA 방식대로 공통 빌드 설정을 컨벤션 플러그인으로 추출했습니다. 새 모듈을 만들 때는 아래 플러그인을 조합해 적용합니다.

| 플러그인 ID | 클래스 | 용도 |
|---|---|---|
| `my.android.application` | `AndroidApplicationConventionPlugin` | 앱 모듈 |
| `my.android.library` | `AndroidLibraryConventionPlugin` | 라이브러리 모듈 |
| `my.android.compose` | `AndroidComposeConventionPlugin` | Compose 활성화 |
| `my.android.hilt` | `AndroidHiltConventionPlugin` | Hilt DI |
| `my.android.feature` | `AndroidFeatureConventionPlugin` | feature 모듈 통합 설정 |

> SDK(minSdk/targetSdk/compileSdk) 및 Java/Compose 세부 설정은 위 컨벤션 플러그인(`build-logic/convention/src/main/kotlin/*.kt`) 안에 정의되어 있습니다. SDK 정책을 바꿀 때는 개별 모듈이 아니라 컨벤션 플러그인을 수정하세요.

---

## 5. 핵심 기능 흐름

### 앱 진입 및 네비게이션

`MainActivity`는 `@AndroidEntryPoint`로 Hilt를 연결하고, `QRonTheme` 아래에서 `Scaffold` + `NavigationBar`로 두 개의 하단 탭을 구성합니다.

```
MainActivity → MainScreen
 ├─ [탭 0] ScannerScreen (onExternalScanSelected / onInternalScanSelected)
 └─ [탭 1] HistoryScreen (HistoryViewModel.historyList / clearHistory)
```

⚠️ 현재 `MainScreen`의 스캔 모드 선택 콜백(`onExternalScanSelected`, `onInternalScanSelected`)과 기록 필터 콜백(`onFilterSelected`)은 **빈 람다(TODO 주석)** 로 남아 있습니다. 즉 UI 셸과 화면 전환 골격은 있으나, "외부 스캔 시작 → 카메라 프리뷰 활성화", "화면 내 스캔 안내" 로직의 실제 연결이 미완성입니다.

### 화면 내 스캔 (feature:scan)

`app` 매니페스트와 별개로 `feature/scan` 모듈 매니페스트에 두 서비스가 등록되어 있습니다.

- `QRonTileService` — `BIND_QUICK_SETTINGS_TILE` 권한, 퀵 설정 타일("QR 화면 스캔") 진입점.
- `QRonAccessibilityService` — `BIND_ACCESSIBILITY_SERVICE` 권한, `accessibility_service_config.xml` 연결. 화면 캡처/분석 경로.

이 경로는 안드로이드 버전별로 백그라운드 Activity 실행·포그라운드 서비스·정확한 알람 등 제약이 크므로, 실제 단말에서의 동작 검증과 권한 온보딩 흐름 정비가 필요합니다.

### 기록 저장 흐름

`core:database`(Room)에 `HistoryEntity`/`HistoryDao`가 정의되고, `data`의 `HistoryRepositoryImpl`이 `HistoryMapper`로 엔티티↔도메인 모델(`QRResult`)을 변환하여 `domain`의 `HistoryRepository` 인터페이스를 구현합니다. DI는 `data/di/DataModule`과 `core/database/di/DatabaseModule`에서 바인딩됩니다.

---

## 6. 빌드 & CI/CD

로컬 빌드 및 검증:

```
./gradlew assembleDebug     # 디버그 APK
./gradlew lint              # Lint
./gradlew test              # 단위 테스트
./gradlew bundleRelease     # 릴리스 AAB
```

GitHub Actions (`.github/workflows/`):

- **CI (`ci.yml`)** — `main`/`develop` 대상 PR 시 Java 21에서 Lint → Debug 빌드 → 단위 테스트 실행, Lint 결과를 아티팩트로 업로드.
- **CD (`cd.yml`)** — `main` push 시 Release AAB 빌드 후 30일 보관 아티팩트 업로드. (서명/스토어 배포 단계는 아직 없음.)

---

## 7. 알려진 미완성 항목 (Known Gaps / TODO)

- **네비게이션 콜백 미구현:** `MainScreen`의 외부/내부 스캔 선택, 기록 필터 콜백이 빈 람다 상태.
- **화면 내 스캔 통합 미검증:** 타일→접근성 서비스→화면 QR 인식→결과 표시까지의 엔드투엔드 경로가 실단말에서 검증되지 않음.
- **런타임 권한 온보딩 부재:** CAMERA 및 접근성 서비스 활성화 유도 UX가 명확히 정리되지 않음.
- **테스트 부재:** JUnit/Espresso/Compose 테스트 의존성은 카탈로그에 정의됐으나 실제 테스트 코드가 없음.
- **결과 화면 결합도:** `feature:scanner`가 `feature:result`에 직접 의존 → 공용 네비게이션 계층으로 대체 검토.
- **릴리스 서명 미구성:** CD가 서명되지 않은 AAB만 생성.

---

## 8. 다음 작업 추천 (Suggested Next Steps)

1. `MainScreen`의 스캔 모드 선택 콜백을 실제 동작(카메라 프리뷰 시작, 화면 내 스캔 안내)과 연결한다.
2. `feature:scan`의 타일·접근성 서비스 경로를 실단말에서 검증하고, 권한 온보딩 플로우를 정비한다.
3. 스캔 → 결과(`ScanResultActivity`) → 기록 저장(`HistoryRepository`)까지의 전체 흐름을 통합 테스트한다.
4. 기록 화면의 필터 기능을 구현한다.
5. `domain`/`data` 계층부터 단위 테스트를 추가해 CI 테스트 단계를 실질화한다.
6. 릴리스 서명 구성 및 스토어 배포 파이프라인을 CD에 추가한다.

---

## 9. 개발 & 협업 규칙 요약

상세 규칙은 `AGENTS.md`에 정의되어 있으며 핵심만 요약하면:

- **선 조사 → 후 계획 → 후 개발.** 코드 변경 전 전체 실행 경로(호출부·피호출부 포함)를 추적하고, 마크다운 계획서를 작성해 승인 후 착수.
- **domain은 순수 코틀린 유지.** `android.*` 의존성 절대 금지.
- **의존성은 Version Catalog로만 추가.** `build.gradle`에 하드코딩 금지.
- **커밋은 Conventional Commits** (`feat`, `fix`, `refactor`, `chore`, `docs`, `style`) 규격, 목적이 다른 변경은 분리 커밋.
- **브랜치 네이밍:** `타입/짧은설명` (예: `feat/diet-tracking-ui`).
- **UI는 Compose + 상태 호이스팅 + 다중 `@Preview`,** 문자열은 `strings.xml`.

---

### 부록: 이 문서 작성 시 확인한 소스

`AGENTS.md`, `CLAUDE.md`, `README.md`, `settings.gradle.kts`, 전체 `build.gradle.kts`(app·domain·data·core·feature·build-logic), `gradle/libs.versions.toml`, `app` 및 `feature/scan` `AndroidManifest.xml`, `MainActivity.kt`, `QRonApplication.kt`, GitHub Actions 워크플로(`ci.yml`, `cd.yml`), git 커밋 이력. 각 feature/data/core 모듈의 내부 `.kt` 구현 로직은 파일 역할·이름·DI 배선·매니페스트 등록 기준으로 상태를 판단했습니다.
