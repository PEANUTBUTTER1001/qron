# android-base-template

최신 안드로이드 개발 표준을 반영한 멀티 모듈 기반의 베이스 템플릿 프로젝트입니다. 반복되는 빌드 설정을 `Convention Plugins`로 공통화하여 프로젝트 구축 시간을 단축하고 유지보수성을 극대화했습니다.

## 🚀 주요 특징 (Key Features)

- **Build Logic (Convention Plugins)**: `build-logic` 모듈을 통해 빌드 설정을 모듈화하여 관리합니다.
    - `my.android.application`: 앱 모듈 기본 설정
    - `my.android.library`: 라이브러리 모듈 기본 설정
    - `my.android.compose`: Jetpack Compose 환경 설정
    - `my.android.hilt`: Hilt 의존성 주입 설정
- **Modern Tech Stack**: Kotlin 2.3.10, AGP 9.0.1, Compose 등 최신 스택 사용.
- **Version Catalog**: `libs.versions.toml`을 통해 모든 라이브러리 및 플러그인 버전을 중앙 집중식으로 관리합니다.
- **CI/CD**: GitHub Actions를 통해 빌드, 테스트(CI) 및 배포(CD) 워크플로우를 자동화했습니다.

## 🛠 기술 스택 (Tech Stack)

### Core
- **Language**: Kotlin 2.3.10
- **JDK**: Java 21
- **Gradle**: 9.2.1
- **Android Gradle Plugin (AGP)**: 9.0.1
- **Min SDK**: 26 / **Target SDK**: 36

### UI & Architecture
- **Jetpack Compose**: BOM 2026.02.01 기반
- **DI**: Hilt 2.59.2
- **Annotation Processing**: KSP 2.3.5
- **Material 3**: 최신 Material Design UI 컴포넌트

### Testing
- **Unit Test**: JUnit 4
- **UI Test**: Espresso 3.7.0, Compose UI Test


## ⚙️ 설정 및 사용법 (Getting Started)

### 1. 개발 환경
- **Android Studio**: Ladybug (2024.2.1) 이상 권장
- **JDK**: Java 21 필수 설정 (Project Structure > SDK Location)

### 2. 신규 모듈 추가 시 사용 예시
새로운 모듈을 생성한 후 `build.gradle.kts`에 필요한 컨벤션 플러그인을 적용하면 복잡한 설정 없이 즉시 개발이 가능합니다.


## 🤖 CI/CD 워크플로우
- **CI (Build & Test)**: 모든 Pull Request 시 실행되어 빌드 성공 여부와 테스트 통과를 확인합니다.
- **CD (Release)**: `main` 브랜치에 푸시되거나 릴리스 태그 생성 시 배포 프로세스를 수행합니다.

---