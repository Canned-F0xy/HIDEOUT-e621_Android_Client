# 🦊 HIDEOUT (e621 Android Client)

![Version](https://img.shields.io/badge/Version-2026--07--10-FF6B00?style=for-the-badge)
![License](https://img.shields.io/badge/License-GPL_3.0-blue?style=for-the-badge)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![WireGuard](https://img.shields.io/badge/WireGuard-881798?style=for-the-badge&logo=wireguard&logoColor=white)
![Downloads](https://img.shields.io/github/downloads/Canned-F0xy/HIDEOUT/total.svg?style=for-the-badge&color=yellow&label=다운로드)

![26-07-28_12-17-40-881.png](https://github.com/user-attachments/assets/c06c40ec-0ec1-426c-adc2-8fbef78dbbcc)

**HIDEOUT**은 제약 없이 안전하게 e621을 탐색할 수 있도록 설계된 안드로이드 전용 클라이언트 앱입니다.

Jetpack Compose 기반의 수려한 UI와 더불어, 강력한 **내장 VPN 터널링 기능(Mullvad & WireGuard)** 과 **Cloudflare 403 캡차 우회 기능**을 앱 내부에 완벽하게 탑재하여 국가 차단 및 봇 방어막을 자체적으로 돌파합니다.

**HIDEOUT** is a dedicated Android client app designed to securely explore e621 without restrictions.

Along with a sleek UI based on Jetpack Compose, it completely integrates a **built-in VPN tunneling feature (Mullvad & WireGuard)** and **Cloudflare 403 Captcha bypass** directly into the app, breaking through regional blocks and bot defenses on its own.

## 주요 기능 (Key Features)

* **강력한 인앱 자체 VPN:** 외부 VPN 앱을 켤 필요 없이 앱 내부에서 암호화 우회 터널을 생성합니다.
  * **Mullvad VPN 자동 연결:** Mullvad 유료 계정(16자리 숫자)을 입력하면 API를 통해 즉시 일본(JP) 서버로 자동 연결됩니다.
  * **WireGuard `.conf` 업로드:** Proton VPN 등에서 발급한 개인 .conf 파일을 등록하는 방식도 완벽히 지원합니다.

* **Cloudflare 403 완벽 우회:** 403 차단 감지 시 내장 브라우저 팝업을 통해 캡차를 풀고, 쿠키(`cf_clearance`)를 가로채어 앱 내 모든 통신 및 다운로드에 적용합니다.

* **무한 스크롤 자동완성:** 검색창을 덮지 않는 인라인 카드 형태의 부드러운 추천 태그 스크롤을 지원합니다.

* **계정 동기화 및 즐겨찾기:** e621 계정 연동(Username & API Key) 및 실시간 즐겨찾기 동기화를 지원합니다. (중요정보는 암호화되어 안전하게 보관됩니다.)

* **미디어 뷰어:** 스와이프 페이징, 핀치 줌 전체화면, WebM/MP4 비디오 플레이어(ExoPlayer)를 내장하고 있습니다.

* **맞춤형 필터링:** R-18 모드 온/오프 및 개인화된 블랙리스트(기피 태그) 차단 기능을 제공합니다.

<!-- -->

* **Powerful In-App VPN:** Generates an encrypted bypass tunnel directly within the app without needing external VPN apps.
  * **Mullvad VPN Auto Connect:** Enter your 16-digit Mullvad paid account number to automatically connect to a Japan (JP) server via API.
  * **WireGuard `.conf` Upload:** Fully supports uploading personal config files extracted from services like Proton VPN.
  
* **Seamless Cloudflare 403 Bypass:** When a 403 block is detected, it resolves the captcha via a built-in browser popup, intercepts the cookie (`cf_clearance`), and automatically applies it to all network requests and downloads within the app.
  
* **Infinite Scroll Autocomplete:** Supports smooth, inline card-style scrolling for tag suggestions without obstructing the search bar.
  
* **Account Sync & Favorites:** Supports e621 account integration (Username & API Key) and real-time favorites syncing. (Sensitive info is securely encrypted and stored.)
  
* **Media Viewer:** Features swipe paging, pinch-to-zoom fullscreen viewing, and a built-in WebM/MP4 video player powered by ExoPlayer.
  
* **Custom Filtering:** Provides an R-18 mode toggle and personalized blacklist blocking for avoided tags.

## 기술 스택 (Tech Stack)

* **UI:** Jetpack Compose, Material Design 3
* **Network:** Retrofit2, OkHttp3, Coil (GIF/WebP support)
* **Media:** AndroidX Media3 (ExoPlayer)
* **Security:** EncryptedSharedPreferences
* **VPN Core:** `wireguard-android` (wireguard-go JNI wrapper), Android VpnService, Mullvad API

## 설치 및 사용 방법 (How to Use)

1. Release 탭에서 최신 버전의 `.apk` 파일을 다운로드하여 안드로이드 기기에 설치합니다.

2. 앱 첫 실행 시 나타나는 초기 화면에서 원하는 우회 방식을 선택합니다.
   * **Mullvad VPN:** 결제된 16자리 계정 번호를 입력하여 간편하게 연결합니다.
   * **WireGuard conf:** 본인의 설정 파일(`.conf`)을 선택하여 등록합니다.
   *(이미 Proton VPN 등 외부 VPN을 사용 중이라면 '설정 안함'을 눌러 통과할 수 있습니다.)*

3. '연결하기' 또는 '터널 활성화' 버튼을 눌러 우회 터널을 뚫고 앱을 사용합니다. 상단 바 메뉴를 통해 언제든 **VPN 설정 / VPN 해제**가 가능합니다.

4. **e621 로그인:** 원활한 사용을 위해 좌측 상단 메뉴 -> `계정 로그인`에서 e621 Username과 API Key를 입력하는 것을 권장합니다.

<!-- -->

1. Download the latest `.apk` file from the Release tab and install it on your Android device.
   
2. Launch the app and select your preferred VPN bypass method on the initial setup screen:
   * **Mullvad VPN:** Enter your valid 16-digit paid account number for a quick connection.
   * **WireGuard conf:** Register your own configuration file (`.conf`).
   *(If you are already using an external VPN like Proton VPN, you can bypass this step by tapping 'Skip Setup'.)*
   
3. Tap 'Connect' or 'Enable Tunnel' to establish the bypass connection and start using the app. You can easily **Enable VPN / Disable VPN** anytime via the top bar menu.
   
4. **e621 Login:** For seamless usage, it is highly recommended to enter your e621 Username and API Key via the top-left menu -> `Account Login`.

## 주의사항 및 면책 조항 (Disclaimer)

* 본 앱은 e621.net의 API를 활용하는 서드파티 클라이언트이며, 공식 앱이 아닙니다.

* 앱을 통해 접근하는 모든 콘텐츠의 책임은 사용자 본인에게 있으며, 개발자는 사용자가 열람하거나 다운로드한 콘텐츠에 대해 어떠한 법적 책임도 지지 않습니다.

* 거주 국가의 법률을 준수하여 사용하시기 바랍니다.

<!-- -->

* This app is a third-party client utilizing the e621.net API and is not an official app.
  
* Users are solely responsible for any content accessed through this app. The developer assumes no legal liability for the content viewed or downloaded.
  
* Please use this app in strict compliance with the laws of your country of residence.

## 라이선스 (License)

이 프로젝트는 **GNU General Public License v3.0 (GPL 3.0)** 에 따라 배포됩니다.

자세한 내용은 [LICENSE](LICENSE) 파일을 확인해 주세요.

This project is licensed under the **GNU General Public License v3.0 (GPL 3.0)**.

See the [LICENSE](LICENSE) file for more details.
