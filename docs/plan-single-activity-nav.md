# 구현 계획서: 단일 Activity + Compose Navigation 전환 및 Route/Screen 분리

> 브랜치 제안: `refactor/single-activity-nav` · 기준: `develop` (HEAD `a5f28e6`) · 작성일 2026-07-02
> 목적: (1) 인앱 화면 전환을 `startActivity` → Compose Navigation destination으로 전환, (2) 모든 feature를 `Route(stateful)` / `Screen(stateless)` / `ViewModel(단일 UiState)` 3계층으로 표준화.

---

## 1. 기능 요약

| 항목 | 변경 전 (As-Is) | 변경 후 (To-Be) |
|---|---|---|
| Activity 수 | 2개 (`MainActivity`, `ScanResultActivity`) | 1개 (`MainActivity`) — 단일 Activity |
| 인앱 화면 전환 | `ScannerScreen` → `startActivity(ScanResultActivity)` | `NavHost` destination 이동 (`result/{id}`) |
| 화면 셸 | `MainScreen`의 `selectedTab` 수동 상태 | `NavHost` + 하단탭이 top-level destination 전환 |
| Route/Screen | 혼재 (`ScannerScreen`이 VM·Context·권한 다 처리) | `XxxRoute`(stateful) + `XxxScreen`(stateless) 분리 |
| 결과 화면 테마 | `MaterialTheme {}` (불일치) | `QRonTheme {}` 통일 |
| feature 간 의존 | `feature:scanner` → `feature:result` | 제거 (네비게이션은 route로 위임) |
| ViewModel 상태 | 널러블/원시 타입 노출 | 단일 `sealed`/`data class` UiState + `StateFlow` |
| 상태 수집 | `collectAsState()` | `collectAsStateWithLifecycle()` |

---

## 2. 결정 필요 사항 (Decisions Required)

아래 4가지는 구현 방향을 크게 바꾸므로 **착수 전 결정**이 필요합니다. 각 항목에 추천안(✅)을 표시했습니다.

> **확정됨 (2026-07-02):** D1 = Type-safe Navigation · D2 = 일반 destination + ModalBottomSheet · D3 = 인앱만(외부 진입 제외) · D4 = History 필터 포함. → 모두 추천안 채택. 이 계획서는 승인 시 §4 파일 목록/§7 커밋 전략대로 착수 가능.

### D1. 네비게이션 방식

| 방식 | 장점 | 단점 | 추천 |
|---|---|---|---|
| Type-safe Navigation (`@Serializable` 라우트, NiA식) | 컴파일 타임 타입 안전, 인자 안전, NiA 정합 | `kotlin-serialization` 플러그인 + 의존성 추가 | ✅ |
| String 라우트 (`"result/{id}"`) | 추가 의존성 없음, 단순 | 문자열 오타·인자 파싱 런타임 위험 | (Fallback) |
| 기존 수동 전환 유지 | 변경 최소 | 단일 Activity 원칙·백스택 문제 미해결 | |

> Fallback: type-safe 도입 시 빌드 이슈가 생기면 String 라우트로 즉시 대체 가능(코드 구조 동일).

### D2. 결과 화면 표현 방식

현재 결과는 `ModalBottomSheet`입니다. destination으로 옮길 때:

| 방식 | 장점 | 단점 | 추천 |
|---|---|---|---|
| 일반 `composable` destination 내부에서 `ModalBottomSheet` 렌더, dismiss 시 `popBackStack()` | **추가 의존성 없음**, M3 그대로 사용 | destination 진입 시 시트 애니메이션 직접 관리 | ✅ |
| 전용 `bottomSheet {}` destination | 시트 전환이 네이티브하게 자연스러움 | `androidx.compose.material:material-navigation` 추가 의존성 | |
| 풀스크린 `composable` destination | 가장 단순 | 기존 바텀시트 UX 상실 | |

### D3. 외부 진입(QS 타일 / 접근성 서비스) 통합 범위

`feature:scan`의 타일·접근성 경로는 아직 동작 검증 전입니다.

| 범위 | 내용 | 추천 |
|---|---|---|
| 이번 스코프 제외 | 인앱 플로우만 리팩터링. 외부 진입 결과 표시는 `feature:scan` 완성 시 `MainActivity` deep link로 별도 처리 | ✅ |
| 이번 스코프 포함 | 타일→`MainActivity`(Intent extra/deep link)→result destination까지 함께 구현 | |

> 제외를 추천하는 이유: `feature:scan`의 실단말 동작이 미검증이라, 리팩터링과 신규 기능 검증을 한 커밋에 섞으면 위험(§AGENTS 변경 목적 분리 원칙).

### D4. History 필터 구현

`onFilterSelected` 콜백이 현재 어디에도 연결되지 않은 상태입니다(dangling).

| 범위 | 내용 | 추천 |
|---|---|---|
| 이번에 포함 | 필터 상태를 `HistoryViewModel`로 올리고 `combine`으로 목록 필터링, `HistoryScreen` stateless 완성 | ✅ |
| 별도 작업 | 이번엔 구조만 잡고 필터 로직은 후속 이슈로 분리 | |

### D5. (확인) 네비게이션 라이브러리 버전

`androidx.navigation:navigation-compose`는 현재 카탈로그에 없어 신규 추가가 필요합니다. 핀 고정된 Compose BOM(`2026.05.01`)과 호환되는 **최신 안정 버전을 추가 시점에 확정**합니다(임의 버전 하드코딩 금지 — §AGENTS 의존성 규칙). 이 항목은 결정보다는 착수 시 검증 사항입니다.

---

## 3. 데이터 흐름도

### As-Is (현재)

```
MainActivity(MainScreen, selectedTab)
   ├─ tab0: ScannerScreen(hiltViewModel) ──scanResultEvent──▶ startActivity(ScanResultActivity) ⨯ 별도 Activity/백스택
   │                                                              └─ ScanResultViewModel(SavedStateHandle "QR_RESULT_ID")
   └─ tab1: hiltViewModel + collectAsState ──▶ HistoryScreen(stateless)   ← route 로직이 MainActivity에 누수
```

### To-Be (전환 후, D1=type-safe / D2=옵션1 기준)

```
MainActivity(QRonTheme { AppNavHost(navController) })
   │
   ├─ [top-level] Scanner  : ScannerRoute(onNavigateToResult) ─┐
   │      └ ScannerScreen(stateless: hasPermission, onFrameAnalyzed, ...)
   │                                                            │
   ├─ [top-level] History  : HistoryRoute()                    │ navController.navigate(Result(id))
   │      └ HistoryScreen(stateless: uiState, onFilter, onClear)│
   │                                                            ▼
   └─ [destination] Result(id) : ResultRoute(onDismiss=popBackStack)
          └ ResultContent(stateless)   ← SavedStateHandle는 nav argument로 자동 주입
```

핵심: 스캔 성공 이벤트는 `ScannerViewModel`의 `SharedFlow<Long>` → `ScannerRoute`에서 수집 → `onNavigateToResult(id)` → `NavHost`가 result destination으로 이동. `startActivity` 및 `feature:scanner`→`feature:result` 의존성 제거.

---

## 4. 변경 파일 목록

경로는 저장소 루트 기준입니다. 태그: `[NEW]` 신규 · `[MODIFY]` 수정 · `[DELETE]` 삭제.

### Build / Catalog

| 태그 | 파일 | 변경 내용 |
|---|---|---|
| [MODIFY] | [gradle/libs.versions.toml](gradle/libs.versions.toml) | `navigation-compose` (D1=type-safe면 `kotlin-serialization` 플러그인·`kotlinx-serialization-json`) 추가, (D2=옵션2면 `material-navigation`) |
| [MODIFY] | [app/build.gradle.kts](app/build.gradle.kts) | navigation-compose 의존성 추가 |
| [MODIFY] | [feature/scanner/build.gradle.kts](feature/scanner/build.gradle.kts) | `implementation(project(":feature:result"))` **제거**, navigation-compose 추가 |
| [MODIFY] | [feature/result/build.gradle.kts](feature/result/build.gradle.kts) | navigation-compose 추가 (route 정의용) |
| [MODIFY] | [feature/history/build.gradle.kts](feature/history/build.gradle.kts) | navigation-compose 추가 |

### App (진입/네비게이션)

| 태그 | 파일 | 변경 내용 |
|---|---|---|
| [MODIFY] | [app/.../MainActivity.kt](app/src/main/java/com/peanutbutter1001/qron/MainActivity.kt) | `MainScreen`의 수동 `selectedTab` 제거, `QRonTheme { AppNavHost() }` 로 교체 |
| [NEW] | app/.../navigation/AppNavHost.kt | `NavHost` + top-level 탭 + result destination 정의 |
| [NEW] | app/.../navigation/AppDestinations.kt | (D1=type-safe면 `@Serializable` route 타입 / String이면 route 상수) |

### Feature: scanner

| 태그 | 파일 | 변경 내용 |
|---|---|---|
| [NEW] | feature/scanner/.../ScannerRoute.kt | stateful: `hiltViewModel`, 권한 런처, 이벤트 수집 → `onNavigateToResult` |
| [MODIFY] | [feature/scanner/.../ScannerScreen.kt](feature/scanner/src/main/java/com/peanutbutter1001/qron/feature/scanner/ScannerScreen.kt) | stateless화: VM/Context/Intent 제거, 콜백 파라미터화, `@Preview` 추가 |
| [MODIFY] | [feature/scanner/.../ScannerViewModel.kt](feature/scanner/src/main/java/com/peanutbutter1001/qron/feature/scanner/ScannerViewModel.kt) | 변경 최소(이벤트 흐름 유지). 필요 시 UiState 도입 |

### Feature: result

| 태그 | 파일 | 변경 내용 |
|---|---|---|
| [DELETE] | [feature/result/.../ScanResultActivity.kt](feature/result/src/main/java/com/peanutbutter1001/qron/feature/result/ScanResultActivity.kt) | Activity 제거 (내용은 Route/Screen으로 이전) |
| [NEW] | feature/result/.../ResultRoute.kt | stateful: `hiltViewModel`, `collectAsStateWithLifecycle`, `onDismiss` |
| [NEW] | feature/result/.../ResultScreen.kt | 기존 `ResultContent` + `ModalBottomSheet` 래핑 (stateless) |
| [MODIFY] | [feature/result/.../ScanResultViewModel.kt](feature/result/src/main/java/com/peanutbutter1001/qron/feature/result/ScanResultViewModel.kt) | `QRResult?` → `sealed interface ResultUiState { Loading/Success/NotFound }` |
| [MODIFY] | [feature/result/src/main/AndroidManifest.xml](feature/result/src/main/AndroidManifest.xml) | `ScanResultActivity` 등록 제거 |

### Feature: history

| 태그 | 파일 | 변경 내용 |
|---|---|---|
| [NEW] | feature/history/.../HistoryRoute.kt | stateful: `hiltViewModel`, 상태 수집 → `HistoryScreen` 연결 |
| [MODIFY] | [feature/history/.../HistoryScreen.kt](feature/history/src/main/java/com/peanutbutter1001/qron/feature/history/HistoryScreen.kt) | `uiState` 기반으로 정리, `@Preview` 추가 |
| [MODIFY] | [feature/history/.../HistoryViewModel.kt](feature/history/src/main/java/com/peanutbutter1001/qron/feature/history/HistoryViewModel.kt) | (D4=포함 시) 필터 상태 `StateFlow` + `combine` 필터링 |

> 결과 화면을 top-level 탭이 아닌 result destination으로만 진입하도록 하면, app이 `feature:result`를 계속 의존하는 것은 정상(NavHost가 result Route를 호출). `feature:scanner`만 result 직접 의존을 끊습니다.

---

## 5. 핵심 코드 변경 (의사 코드)

### 5.1 단일 Activity + NavHost

```kotlin
// MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(...) {
    enableEdgeToEdge()
    setContent { QRonTheme { AppNavHost() } }   // ← QRonTheme 통일
  }
}

// AppNavHost.kt  (D1=type-safe 예시)
@Composable fun AppNavHost(nav: NavHostController = rememberNavController()) {
  Scaffold(bottomBar = { QRonBottomBar(nav) }) { pad ->
    NavHost(nav, startDestination = Scanner, Modifier.padding(pad)) {
      composable<Scanner> {
        ScannerRoute(onNavigateToResult = { id -> nav.navigate(Result(id)) })
      }
      composable<History> { HistoryRoute() }
      composable<Result> { /* SavedStateHandle 자동 주입 */ ResultRoute(onDismiss = { nav.popBackStack() }) }
    }
  }
}
```

### 5.2 Scanner: Route / Screen 분리

```kotlin
// ScannerRoute.kt (stateful)
@Composable fun ScannerRoute(onNavigateToResult: (Long) -> Unit, vm: ScannerViewModel = hiltViewModel()) {
  val ctx = LocalContext.current
  // 권한 상태/런처는 Route로 호이스팅
  LaunchedEffect(Unit) { vm.scanResultEvent.collect(onNavigateToResult) }  // 이벤트 → 네비게이션
  ScannerScreen(
    hasCameraPermission = ..., onRequestPermission = ...,
    onFrameAnalyzed = vm::processImageProxy, onResume = vm::resumeScanning,
    onOpenAccessibilitySettings = { ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
  )
}
// ScannerScreen.kt (stateless) — VM/Navigation/Intent 참조 없음, @Preview 가능
```

### 5.3 Result: Activity → Route + Screen

```kotlin
// ResultViewModel: 단일 UiState
sealed interface ResultUiState { data object Loading: ResultUiState
  data class Success(val qr: QRResult): ResultUiState; data object NotFound: ResultUiState }
// id는 nav argument → SavedStateHandle 로 자동 주입 (기존 "QR_RESULT_ID" 키 로직 재사용 가능)

// ResultRoute(onDismiss) → ResultScreen(uiState, onOpenLink, onCopy, onShare, onDismiss)
//   ResultScreen 내부에서 ModalBottomSheet(onDismissRequest = onDismiss) { ResultContent(...) }
```

---

## 6. 검증 계획 (Verification Plan)

| # | 구분 | 시나리오 | 기대 결과 |
|---|---|---|---|
| 1 | 정상 | 카메라 탭에서 QR 인식 | result destination으로 이동, 결과 바텀시트 표시(테마 = QRonTheme) |
| 2 | 정상 | 결과 시트 dismiss | `popBackStack()`으로 스캐너로 복귀, 스캔 재개(`resumeScanning`) |
| 3 | 정상 | 하단탭 스캐너↔기록 전환 | 각 top-level destination 정상 전환, VM 상태 유지 |
| 4 | 정상 | 기록 탭 진입 | 저장된 기록 목록 표시(`collectAsStateWithLifecycle`) |
| 5 | 엣지 | 카메라 권한 거부 | Screen이 권한 요청 UI 표시(콜백만으로 동작, VM 무관) |
| 6 | 엣지 | 잘못된/없는 result id | `ResultUiState.NotFound` 렌더(무한 로딩 없음) |
| 7 | 엣지 | 결과 표시 중 뒤로가기 | 크래시 없이 스캐너 복귀 |
| 8 | 엣지 | URL이 아닌 QR | "Open Link" 버튼 미표시, Copy/Share만 노출 |
| 9 | (D4) 정상 | 기록 필터 칩 선택 | 해당 타입만 필터링되어 목록 갱신 |
| 10 | 호환 | 리팩터링 후 기존 저장 데이터 | Room 스키마 무변경 → 기존 기록 그대로 로드 |
| 11 | 미리보기 | `@Preview` 렌더 | Scanner/History/Result Screen이 VM 없이 Preview 렌더 성공 |
| 12 | 빌드 | `./gradlew lint assembleDebug test` | 성공, 미사용 의존성/참조 경고 없음 |

> 데이터 계층(domain/data/core:database)은 변경하지 않으므로 마이그레이션 불필요(시나리오 #10). `feature:scan`(타일/접근성) 경로는 D3=제외 기준에서 이번 변경 대상 아님.

---

## 7. 커밋 전략 (Conventional Commits, 목적별 분리)

의존성 방향(Domain → Data → Feature → App) 및 변경 목적에 따라 순서대로 분리합니다.

1. `chore(build): navigation-compose 의존성 및 버전 카탈로그 추가` — (D1/D2에 따라 serialization·material-navigation 포함)
2. `refactor(result): ScanResultActivity 제거 및 Route/Screen 분리, UiState 도입` — Activity → destination
3. `refactor(scanner): Route/Screen 분리 및 feature:result 의존성 제거`
4. `refactor(history): Route 신설 및 Screen stateless 정리` — (D4=포함 시 필터 로직 동일 커밋 또는 후속 `feat(history): 기록 타입 필터`)
5. `refactor(app): 단일 Activity + AppNavHost 도입, 수동 탭 상태 제거`
6. `fix(result): 결과 화면 테마 QRonTheme 통일` — (2에 병합 가능)

각 커밋 후 `./gradlew assembleDebug`로 컴파일 가능한 상태 유지를 권장합니다.

---

## 부록: 참고 규칙 (AGENTS.md)

- domain 순수 코틀린 유지, 의존성은 Version Catalog로만 추가.
- UI는 상태 호이스팅 + 다중 `@Preview`, 문자열은 `strings.xml`.
- 목적이 다른 변경은 커밋 분리. 이 계획은 승인 후 착수(선 계획 → 승인 → 개발).
