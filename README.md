# QRon

온디바이스 QR 스캐너 Android 앱

전면 카메라 실시간 스캔과 퀵 설정 타일을 통한 화면 내 스캔을 지원하며, 모든 인식은 온디바이스에서 처리되어 네트워크 전송이 없습니다.

## 🗂️ 프로젝트 개요

- **앱 이름** : QRon
- **구성원** : 1인 개발
- **작업 기간** : 2026.06 ~ 작업중(배포 준비 중)
- **플랫폼** : Android
- **개발 언어** : Kotlin
- **개발 환경** : Android Studio
- **외부API 및 서비스** : On-device AI Google ML Kit Barcode Scanning

## 🎯 서비스 목표

일반 카메라 스캔뿐 아니라, 다른 앱 화면에 떠 있는 QR도 앱 전환 없이 그 자리에서 인식할 수 있는 QR 스캐너를 제공합니다.

## 🧰 기술 스택

| 분류           | 기술                                                            |
|--------------|---------------------------------------------------------------|
| Language     | Kotlin                                                        |
| Architecture | MVVM (Clean Architecture, Multi-module)                       |
| Asynchronous | Coroutine, Flow                                               |
| UI           | Jetpack Compose (Material3), Navigation                       |
| On-device AI | Google ML Kit (Barcode Scanning), CameraX                     |
| DataBase     | Room                                                          |
| DI           | Hilt                                                          |
| BUILD        | Gradle Version Catalog(libs.versions.toml), Convention Plugin |
| ETC / Tools  | Android Studio, GitHub, GitHub Actions (CI/CD), SourceTree    |

## ✨ 서비스 주요 기능

- 전면/후면 카메라 기반 실시간 QR 인식 (CameraX + ML Kit Barcode Scanning), 핀치 줌 지원
- 화면 내 스캔: 퀵 설정 타일 → 접근성 서비스 화면 캡처 → QR 인식으로, 다른 앱 화면의 QR도 앱 전환 없이 인식
- QR 타입별 파싱: URL, WiFi, 연락처(Contact), 텍스트, 결제(Payment) 등
- 스캔 기록 영속화 (Room): 출처(카메라/화면) 필터, 전체 삭제(확인 다이얼로그)
- 결과 화면에서 인식된 원본 값과 QR 크롭 이미지 표시
- 모든 인식과 저장을 온디바이스에서 처리, 네트워크 전송 없음

## 🔍 스캔 방식

| 스캔 방식               | 설명                                               |
|---------------------|--------------------------------------------------|
| 카메라 스캔 (External)   | CameraX 프리뷰 위에서 ML Kit로 QR을 실시간 인식하는 일반 스캔       |
| 화면 내 스캔 (In-screen) | 상단 퀵 설정 타일을 눌러 현재 화면에 표시된 QR을 캡처·인식 (접근성 서비스 활용) |

## 🏷️ QR 타입 파싱

| 타입      | 설명              |
|---------|-----------------|
| URL     | 웹 링크            |
| WIFI    | 와이파이 접속 정보      |
| CONTACT | 연락처 정보          |
| TEXT    | 일반 텍스트          |
| PAYMENT | 결제 관련 코드        |
| UNKNOWN | 위 타입에 해당하지 않는 값 |

## 📖 사용법

### 1. 카메라 스캔

앱 실행 시 카메라 권한을 요청합니다. 허용 후 스캐너 화면에서 QR을 비추면 실시간으로 인식되어 결과 화면으로 이동합니다.

### 2. 화면 내 스캔

1. 앱 실행 후 **접근성 설정**(시스템 설정 > 접근성 > 설치된 서비스)에서 `QRon`의 접근성 서비스를 활성화합니다. (화면 캡처 권한)
2. 상단 퀵 설정 패널에 **QR 화면 스캔** 타일을 추가합니다.
3. QR이 표시된 화면에서 타일을 누르면 현재 화면을 캡처해 QR을 인식합니다.

### 3. 스캔 기록

기록 화면에서 지난 스캔 결과를 확인하고, 출처(카메라/화면)별로 필터링하거나 전체 삭제할 수 있습니다.

## 🏗️ 프로젝트 아키텍쳐

Now in Android(NiA)의 다중 모듈 클린 아키텍처를 지향합니다.

```
qron/
├─ app/                   앱 진입점, MainActivity, 네비게이션 그래프
├─ domain/                Entity, Repository Interface, UseCase
├─ data/                  도메인 인터페이스 구현 + Hilt 바인딩   
├─ core/designsystem/     Compose 테마(색·타이포) 
├─ core/vision/           ML Kit Barcode Scanning 데이터소스 
├─ core/database/         Room DB, DAO, Entity
├─ feature/history/       스캔 기록 목록·필터 화면    
├─ feature/scanner/       CameraX 실시간 스캔 화면       
├─ feature/result/        스캔 결과 화면   
├─ feature/scan/          화면 내 스캔 (퀵 타일 + 접근성 서비스)  
└── build-logic/          Convention Plugin (공통 빌드 설정)
```

레이어 의존 방향은 app → feature → domain 이며, data가 domain의 인터페이스를 구현하는 의존 역전 구조구성입니다.

## 🔐 필요 권한

| 권한       | 용도                                         |
|----------|--------------------------------------------|
| `CAMERA` | 카메라로 QR 실시간 인식                             |
| 접근성 서비스  | 화면 내 스캔 시 현재 화면 캡처 및 분석 (사용자가 설정에서 직접 활성화) |
