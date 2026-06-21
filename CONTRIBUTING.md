# Contributing to DdalkkakWatch

기여 환영합니다. 워치 한 번 눌러 Mac에 받아쓰기를 띄우는 작은 도구예요.

## 빌드

```bash
# 요구: JDK 17, Android SDK (platform 34, build-tools 34)
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=$HOME/Library/Android/sdk

./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

워치 연결 / 설치 / 셋업 전체 흐름은 온보딩 마법사(`onboarding/`)의 `paths/c1.html`을 참고하세요.

## 프로젝트 구조

```
app/src/main/java/io/gapalja/ddalkkakwatch/
├── MainActivity.kt          # 화면 PTT (꾹 누름 = Opt+Cmd+X, 더블탭 = Enter)
├── QuickDictateActivity.kt  # Knock-Knock 무화면 진입점
├── DdalkkakApp.kt           # Application — BleHidManager 싱글톤
├── ble/
│   ├── BleHidManager.kt     # BluetoothHidDevice 등록·연결·키 전송
│   └── HidReportDescriptor.kt  # USB HID Boot Keyboard descriptor + 키코드
├── haptic/HapticFeedbackController.kt
└── service/DictationService.kt  # BLE 연결 백그라운드 유지 (Foreground)
onboarding/                  # 셋업 마법사 (정적 HTML/JS — GitHub Pages용)
```

## PR 가이드

- **작은 PR** 환영. 하나의 PR = 하나의 관심사.
- 빌드 통과(`./gradlew assembleDebug`) 확인 후 올려주세요.
- 키코드·HID 변경 시 실제 Mac에서 입력 검증한 내용을 PR 설명에 남겨주세요.
- 커밋 메시지: `feat:`, `fix:`, `docs:` 등 conventional prefix 권장.

## 새 마이크 path / 받아쓰기 앱 추가

온보딩 마법사 `onboarding/app.js`의 `PATHS` 객체 + `recommend()` 점수표에 추가하고,
`onboarding/paths/`에 가이드 페이지를 만들어 링크하세요.

## 라이선스

기여하신 코드는 [Apache License 2.0](LICENSE)으로 배포됩니다.
WearMouse 등 외부 출처는 [NOTICE](NOTICE)에 명시합니다.
