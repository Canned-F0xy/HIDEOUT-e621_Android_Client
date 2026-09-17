# 🦊 HIDEOUT (e621 Android Client)

![Version](https://img.shields.io/badge/Version-2026--09--17-FF6B00?style=for-the-badge)
![License](https://img.shields.io/badge/License-GPL_3.0-blue?style=for-the-badge)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![WireGuard](https://img.shields.io/badge/WireGuard-881798?style=for-the-badge&logo=wireguard&logoColor=white)
![Downloads](https://img.shields.io/github/downloads/Canned-F0xy/HIDEOUT-e621_Android_Client/total.svg?style=for-the-badge&color=yellow&label=다운로드)

<img width="3449" height="1708" alt="Image" src="https://github.com/user-attachments/assets/2621aaad-7263-4eca-92dd-0e0e60d62a9b" />
<img width="1440" height="4871" alt="Image" src="https://github.com/user-attachments/assets/a4967b32-cbf0-4761-878f-a5888e9c6599" />

**HIDEOUT**은 제약 없이 안전하게 e621을 탐색할 수 있도록 설계된 최상급 안드로이드 전용 클라이언트 앱입니다.

Jetpack Compose 기반의 트렌디한 UI와 더불어, 강력한 **내장 VPN 터널링 기능(Mullvad & WireGuard)** 과 **Cloudflare 403 캡차 우회 기능**을 앱 내부에 완벽하게 탑재하여 국가 차단 및 봇 방어막을 자체적으로 돌파합니다. 또한 **오프라인 캐싱, 딥 내비게이션 복구, 낙관적 UI 업데이트**를 지원하여 데이터 소모를 극한으로 줄이고 딜레이 없는 쾌적한 갤러리 감상을 보장합니다.

**HIDEOUT** is a premium Android client app designed to securely explore e621 without restrictions.

Along with a trendy UI based on Jetpack Compose, it completely integrates a **built-in VPN tunneling feature (Mullvad & WireGuard)** and **Cloudflare 403 Captcha bypass** directly into the app, breaking through regional blocks and bot defenses on its own. It also ensures zero-delay, comfortable browsing and minimizes data consumption by supporting **offline caching, deep navigation restoration, and optimistic UI updates**.

## 주요 기능 (Key Features)

* **모던 UI & 다이내믹 제스처 (Modern UI & Gestures):** 벤토 그리드(Bento Grid) 스타일의 세련된 상세 화면, 플로팅 알약 검색바, 한 손 조작이 편한 바텀 시트(Bottom Sheet)를 적용했습니다. 핀치 줌(Pinch-to-Zoom)을 통해 갤러리 목록을 1~5열까지 실시간으로 리사이징 할 수 있습니다.

* **최상급 사용자 경험 (Pro-level UX):** 인스타그램 스타일의 더블 탭 즐겨찾기(Double-Tap to Like)와 낙관적 UI(Optimistic UI)를 도입해 딜레이 없는 쾌적함을 제공하며, 스켈레톤 슈머(Shimmer) 로딩으로 체감 속도를 극대화했습니다. 

* **완벽한 딥 내비게이션 (Deep Navigation):** 태그를 누르며 꼬리를 물고 끝없이 탐색해도, 뒤로 가기 시 스와이프했던 스크롤 위치와 이전에 보던 포스트를 정확히 기억하고 완벽하게 복구합니다.

* **강력한 인앱 자체 VPN (In-App VPN):** 외부 VPN 앱을 켤 필요 없이 앱 내부에서 암호화 우회 터널을 생성합니다. (설정 파일 및 개인키는 기기 내 최고 보안 등급의 암호화 금고에 보관됩니다.)
  * **Mullvad VPN 자동 연결:** 유료 계정(16자리 숫자)을 입력하면 즉시 일본(JP) 서버로 자동 연결됩니다.
  * **WireGuard `.conf` 업로드:** Proton VPN 등에서 발급한 개인 .conf 파일을 등록하는 방식도 완벽히 지원합니다.

* **데이터 없는 스마트 캐싱 (Offline Smart Caching):** 한 번 로딩된 미디어는 MD5 해시로 기기에 자동 저장됩니다. 비행기 모드이거나 VPN이 끊긴 오프라인 상태에서도 데이터 소모 없이 쾌적하게 갤러리를 감상할 수 있습니다. 구형 SWF 플래시 파일은 지원되지 않음을 알리는 전용 네온 UI로 똑똑하게 대체(Fallback)하여 렌더링합니다.

* **끊김 없는 다운로드 & 프라이버시 보호 (Smart Download):** 대용량 파일 다운로드 중 네트워크가 끊겨도 이어받기(HTTP Range)를 지원합니다. 화면에 로딩된 미디어는 데이터 소모 없이 즉시 저장되며, `.nomedia` 방어막을 통해 다른 갤러리 앱으로부터 유저의 프라이버시를 완벽히 보호합니다.

* **미디어 뷰어 & 생태계 연동 (Media Viewer & Ecosystem):** e621 댓글(Comments) 확인 및 묶음 앨범(Pools), 부모/자식(Parent/Child) 연관 포스트를 원클릭으로 모아볼 수 있습니다. 썸네일 영상 길이 표기, 부드러운 좌우 페이징, 스마트 닫기 제스처, 내장 WebM/MP4 비디오 플레이어(ExoPlayer)를 지원합니다.

* **편리한 환경 설정 & 관리 (Management & Updates):** 앱 설정에서 비디오 자동재생 온/오프, 실시간 캐시 용량 관리, 다크 테마 설정이 가능합니다. 개인화된 앱 설정은 Base64로 인코딩 후 순수 `.json` 포맷으로 안전하게 백업 및 복원할 수 있습니다. GitHub 최신 릴리즈 버전을 백그라운드에서 자동 감지하여 업데이트를 알려줍니다.

* **Cloudflare 403 완벽 우회 & 무한 스크롤:** 403 차단 감지 시 캡차를 풀고 쿠키(`cf_clearance`)를 가로채어 이미지 썸네일과 비디오를 포함한 모든 통신 모듈에 즉시 적용합니다. 고성능 필터가 적용된 무한 스크롤은 중복 포스트를 완벽하게 차단하며, e621 계정 연동을 통한 실시간 즐겨찾기 동기화 및 R-18 모드, 블랙리스트(기피 태그) 차단을 제공합니다.

<!-- -->

* **Modern UI & Dynamic Gestures:** Features a sleek Bento Grid-style detail screen, a floating search bar, and thumb-friendly Bottom Sheets. Supports real-time grid resizing (1 to 5 columns) via Pinch-to-Zoom on the main gallery.

* **Pro-level UX:** Enjoy a seamless experience with Instagram-style Double-Tap to Like and Optimistic UI for zero-delay interactions, coupled with Skeleton Shimmer loading for maximum perceived speed.

* **Flawless Deep Navigation:** Dive infinitely into tags; hitting back perfectly restores your exact scroll position and previously viewed posts.

* **Powerful In-App VPN:** Generates an encrypted bypass tunnel directly within the app without needing external VPN apps. (VPN configs and private keys are securely stored in the device's KeyStore.)
  * **Mullvad VPN Auto Connect:** Enter your 16-digit paid account number for a quick automatic connection to a JP server.
  * **WireGuard `.conf` Upload:** Fully supports uploading personal config files from services like Proton VPN.
  
* **Offline Smart Caching:** Once loaded, media is automatically saved to the device via MD5. View previously seen posts offline without data consumption. Unsupported legacy SWF files elegantly fallback to a custom neon warning UI.

* **Resumable Download & Privacy Protection:** Supports resumable downloads (HTTP Range) if the network drops. Cached media saves instantly in 0 seconds. Downloads are securely hidden from other apps via `.nomedia` to protect your privacy.

* **Media Viewer & Ecosystem Integration:** Instantly read user comments, seamlessly navigate dedicated album Pools, and explore Parent/Child related posts with a single click. Features thumbnail duration badges, smart swipe-to-close gestures, and a built-in WebM/MP4 ExoPlayer.

* **Cache Management & Settings:** Easily toggle video autoplay, monitor/clear cache size, and switch themes. Export and import personalized app configurations safely via Base64-encoded `.json` files. Auto-detects the latest GitHub release.
  
* **Cloudflare 403 Bypass & Infinite Scroll:** Detects 403 blocks, bypasses captchas, and injects cookies directly into all network layers (including ExoPlayer and Coil). The optimized infinite scroll perfectly prevents duplicate posts. Supports real-time favorites syncing, an R-18 mode toggle, and personalized blacklist blocking.

## 기술 스택 (Tech Stack)

* **UI:** Jetpack Compose, Material Design 3 (Memory Optimized, Pager, ModalBottomSheet)
* **Network:** Retrofit2, OkHttp3, Coil (GIF/WebP support, Cloudflare bypass injection)
* **Media:** AndroidX Media3 (ExoPlayer)
* **Security:** Android KeyStore (AES/GCM Encryption) + Jetpack DataStore
* **VPN Core:** `wireguard-android` (wireguard-go JNI wrapper), Android VpnService, Mullvad API

## 설치 및 사용 방법 (How to Use)

1. Release 탭에서 최신 버전의 `.apk` 파일을 다운로드하여 안드로이드 기기에 설치합니다. GitHub 최신 릴리즈 버전을 백그라운드에서 자동 감지하여 앱 내 업데이트 알림을 띄워줍니다.

2. 앱 첫 실행 시 나타나는 초기 화면에서 원하는 우회 방식을 선택합니다.
   * **Mullvad VPN:** 결제된 16자리 계정 번호를 입력하여 간편하게 연결합니다.
   * **WireGuard conf:** 본인의 설정 파일(`.conf`)을 선택하여 등록합니다.
   *(이미 외부 VPN을 사용 중이라면 '설정 안함'을 눌러 통과할 수 있습니다.)*

3. '연결하기' 또는 '터널 활성화' 버튼을 눌러 우회 터널을 뚫고 앱을 사용합니다.
상단 알약 모양의 검색바 우측 열쇠(VPN) 모양 아이콘을 통해 언제든 **VPN 설정 / VPN 해제**가 가능합니다.

4. **e621 로그인:** 원활한 사용을 위해 상단 메뉴(햄버거 아이콘) -> `계정 로그인`에서 e621 Username과 API Key를 입력하는 것을 권장합니다.

<!-- -->

1. Download the latest `.apk` file from the Release tab and install it. The app automatically detects new GitHub releases in the background and prompts an in-app update notification.
   
2. Select your preferred VPN bypass method on the initial setup screen:
   * **Mullvad VPN:** Enter your valid 16-digit paid account number for a quick connection.
   * **WireGuard conf:** Register your own configuration file (`.conf`).
   *(If using an external VPN, tap 'Skip Setup'.)*
   
3. Tap 'Connect' or 'Enable Tunnel' to establish the bypass connection.
You can easily toggle the VPN anytime via the key icon on the top floating bar.
   
4. **e621 Login:** For seamless usage, it is highly recommended to enter your e621 Username and API Key via the top-left menu -> `Account Login`.

## 주의사항 및 면책 조항 (Disclaimer)

* 본 앱은 e621.net의 API를 활용하는 서드파티 클라이언트이며, 공식 앱이 아닙니다.

* 앱을 통해 접근하는 모든 콘텐츠의 책임은 사용자 본인에게 있으며, 개발자는 사용자가 열람하거나 다운로드한 콘텐츠에 대해 어떠한 법적 책임도 지지 않습니다.

* 거주 국가의 법률을 준수하여 사용하시기 바랍니다.

<!-- -->

* This app is a third-party client utilizing the e621.net API and is not an official app.
  
* Users are solely responsible for any content accessed through this app.
The developer assumes no legal liability for the content viewed or downloaded.
  
* Please use this app in strict compliance with the laws of your country of residence.

## 라이선스 (License)

이 프로젝트는 **GNU General Public License v3.0 (GPL 3.0)** 에 따라 배포됩니다.

자세한 내용은 [LICENSE](LICENSE) 파일을 확인해 주세요.

This project is licensed under the **GNU General Public License v3.0 (GPL 3.0)**.

See the [LICENSE](LICENSE) file for more details.
