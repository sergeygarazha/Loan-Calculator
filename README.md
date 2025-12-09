MKMPA – Compose Multiplatform Loan Calculator
=============================================

A Kotlin Multiplatform sample that calculates short-term loan terms and submits an application via a mock API.

<img width="300" src="https://github.com/user-attachments/assets/e2ab8f79-943e-4c14-a562-a2366643a147" />
<img width="300" src="https://github.com/user-attachments/assets/c5e22ad7-3283-41cb-9d03-de6d876ee1f4" />


Features
--------
- Loan calculator with sliders for amount (5,000–50,000 USD) and period (7/14/21/28 days).
- Live repayment summary with total due, return date, and interest breakdown.
- Form persistence: last selected amount/period is restored on next launch.
- Submission flow with loading state, success/error banners, and mocked backend (`jsonplaceholder.typicode.com/posts`).
- Light and dark theme support (Material 3).

Project layout
--------------
- `composeApp/` – shared Compose UI and platform entry points.
  - `commonMain/` – UI, state, reducers, repository contracts.
  - `androidMain/`, `iosMain/` – platform-specific clients/preferences.
- `shared/` – additional KMP sources used across targets.
- `iosApp/` – SwiftUI host for iOS builds.
- `docs/media/` – place release-ready screenshots/video captures (see below).

Prerequisites
-------------
- JDK 17+ on macOS/Linux/Windows.
- Android Studio Ladybug+ with Android SDK & emulator for Android builds.
- Xcode 15+ with a simulator for iOS builds.
- Recent Node-capable browser for Web (Wasm/JS).

Build & run
-----------
Android
- Start an emulator or connect a device.
- `./gradlew :composeApp:assembleDebug` to build.
- `./gradlew :composeApp:installDebug` to deploy to the active device.

Desktop (JVM)
- `./gradlew :composeApp:run`

Web
- Wasm (recommended): `./gradlew :composeApp:wasmJsBrowserDevelopmentRun`
- JS (legacy browsers): `./gradlew :composeApp:jsBrowserDevelopmentRun`

iOS
- `./gradlew :composeApp:syncFramework` (first run) to generate the KMP framework.
- Open `iosApp/iosApp.xcodeproj` in Xcode and run on a simulator or device.

Testing & quality
-----------------
- `./gradlew check` runs available multiplatform checks.
- Connected tests are not set up; Android/iOS UI is covered manually via the flows above.

Data & networking
-----------------
- Loan submissions POST to `https://jsonplaceholder.typicode.com/posts` (mock). No secrets or config files are required.

Light/Dark media (handoff requirement)
--------------------------------------
- Capture light and dark theme screenshots or a short screen recording of the loan calculator.
- Save assets to `docs/media/` using these names so links stay stable:
  - `docs/media/loan-calculator-light.png`
  - `docs/media/loan-calculator-dark.png`
  - Optional video: `docs/media/loan-calculator-demo.mp4`
- How to capture:
  - Android: run the Debug build, use `adb exec-out screencap -p > docs/media/loan-calculator-light.png`, then enable dark theme in quick settings and repeat.
  - iOS: run from Xcode, `xcrun simctl io booted screenshot docs/media/loan-calculator-light.png`; toggle Appearance to Dark in the simulator and repeat.
  - Desktop/Web: use built-in OS screenshot tooling and place files in the same folder.

Troubleshooting
---------------
- Gradle daemon memory issues: set `ORG_GRADLE_OPTS="-Xmx4g"` or edit `gradle.properties`.
- Cached state looks stale: clear `~/.gradle/caches` and rerun the Gradle task.
