# 🦊 HIDEOUT (e621 Android Client)

![Version](https://img.shields.io/badge/Version-2026--10--25-FF6B00?style=for-the-badge)
![License](https://img.shields.io/badge/License-GPL_3.0-blue?style=for-the-badge)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![WireGuard](https://img.shields.io/badge/WireGuard-881798?style=for-the-badge&logo=wireguard&logoColor=white)
![Downloads](https://img.shields.io/github/downloads/Canned-F0xy/HIDEOUT-e621_Android_Client/total.svg?style=for-the-badge&color=yellow&label=다운로드)

<img width="3350" height="1632" alt="Image" src="https://github.com/user-attachments/assets/9a02bf83-1591-4b99-89ec-c3406d3b2023" />
<img width="1440" height="4871" alt="Image" src="https://github.com/user-attachments/assets/a4967b32-cbf0-4761-878f-a5888e9c6599" />

**HIDEOUT**은 제약 없이 안전하게 e621을 탐색할 수 있도록 설계된 최상급 안드로이드 전용 클라이언트 앱입니다.

Jetpack Compose 기반의 트렌디한 UI와 더불어, 강력한 **내장 VPN 터널링 기능(Mullvad & WireGuard)** 과 **Cloudflare 403 캡차 우회 기능**을 앱 내부에 탑재하여 국가 차단 및 봇 방어막을 자체적으로 돌파합니다. **VPN 킬스위치, 앱 잠금, 시크릿 모드, 앱 내 보관함**으로 프라이버시를 지키고, **오프라인 캐싱, 화질 선택, 데이터 절약 모드**로 데이터 소모를 줄이며, **일괄 다운로드, 팔로우 태그 알림, 로컬 컬렉션**으로 쾌적한 감상과 관리를 돕습니다.

**HIDEOUT** is a premium Android client app designed to securely explore e621 without restrictions.

Along with a trendy UI based on Jetpack Compose, it integrates a **built-in VPN tunneling feature (Mullvad & WireGuard)** and **Cloudflare 403 Captcha bypass** directly into the app, breaking through regional blocks and bot defenses on its own. It protects your privacy with a **VPN kill switch, app lock, incognito mode, and a private vault**, saves data with **offline caching, quality selection, and data saver mode**, and makes collecting easy with **batch downloads, followed-tag notifications, and local collections**.

## 주요 기능 (Key Features)

* **모던 UI & 다이내믹 제스처 (Modern UI & Gestures):** 벤토 그리드(Bento Grid) 스타일의 세련된 상세 화면, 플로팅 알약 검색바, 한 손 조작이 편한 바텀 시트(Bottom Sheet)를 적용했습니다. 핀치 줌(Pinch-to-Zoom)으로 갤러리 목록을 1~5열까지 실시간 리사이징할 수 있으며, **태블릿·가로 화면에서는 갤러리와 상세 화면을 나란히 보여주는 2단 레이아웃**을 지원합니다.

* **최상급 사용자 경험 (Pro-level UX):** 낙관적 UI(Optimistic UI)와 스켈레톤 슈머(Shimmer) 로딩으로 딜레이 없는 쾌적함을 제공합니다. 썸네일을 길게 눌러 **여러 포스트를 한 번에 선택**하고 일괄 다운로드하거나 컬렉션에 담을 수 있습니다.

* **완벽한 딥 내비게이션 (Deep Navigation):** 태그를 누르며 끝없이 탐색해도, 뒤로 가기 시 스크롤 위치와 이전에 보던 포스트를 정확히 기억하고 복구합니다.

* **강력한 인앱 자체 VPN (In-App VPN):** 외부 VPN 앱 없이 앱 내부에서 암호화 우회 터널을 생성합니다. (설정 파일 및 개인키는 Android KeyStore로 암호화하여 보관합니다.)
  * **Mullvad VPN 자동 연결:** 유료 계정(16자리 숫자)을 입력하면 즉시 일본(JP) 서버로 자동 연결됩니다.
  * **WireGuard `.conf` 업로드:** Proton VPN 등에서 발급한 개인 .conf 파일 등록을 지원합니다.
  * **VPN 킬스위치:** 내장 VPN이 끊긴 동안 e621 통신을 차단하여 실제 IP 노출을 막습니다.
  * **자동 재연결:** VPN이 예기치 않게 끊기면 자동으로 다시 연결합니다.

* **보안 & 프라이버시 (Security & Privacy):**
  * **앱 잠금:** 4자리 PIN과 생체 인증으로 앱을 잠급니다.
  * **화면 보호:** 스크린샷과 최근 앱 미리보기를 차단합니다.
  * **시크릿 모드:** 검색·열람 기록을 남기지 않고, 앱을 끌 때 캐시를 지웁니다.
  * **앱 내 보관함:** 다운로드를 갤러리 대신 앱 내부에만 보관하고, 원할 때 갤러리로 내보낼 수 있습니다.

* **스마트한 검색 & 탐색 (Search & Discovery):**
  * **검색 기록과 필터 칩:** 최근 검색어와 정렬(인기순·즐겨찾기순·랜덤·오래된순)·등급 칩을 검색창에서 바로 사용합니다.
  * **저장된 검색:** 자주 쓰는 검색어에 이름을 붙여 📌 칩으로 한 번에 엽니다.
  * **팔로우 태그 알림:** 팔로우한 태그에 새 포스트가 올라오면 알림으로 알려줍니다.
  * **비슷한 이미지 찾기:** e621 역이미지 검색(IQDB)으로 유사한 포스트를 찾습니다.
  * **태그 정보:** 태그를 길게 누르면 위키 설명과 포스트 수를 확인하고 바로 팔로우·블랙리스트에 추가할 수 있습니다.
  * **열람 기록 & 로컬 컬렉션:** 최근 본 포스트를 다시 찾고, 로그인 없이 기기 안에 폴더별로 모아둘 수 있습니다.

* **미디어 뷰어 & 생태계 연동 (Media Viewer & Ecosystem):** e621 댓글 확인, 묶음 앨범(Pools), 부모/자식(Parent/Child) 연관 포스트를 원클릭으로 모아볼 수 있습니다. Pool 리더 모드로 만화형 콘텐츠를 원래 순서대로 세로 스크롤하며 읽을 수 있고, 내장 WebM/MP4 플레이어(ExoPlayer)는 더블 탭 10초 이동을 지원합니다.

* **추천 & 즐겨찾기 연동 (Votes & Favorites):** 상세 화면에서 추천(👍)·비추천(👎)을 할 수 있으며, **추천하면 e621 즐겨찾기에 자동으로 추가**되어 썸네일에 하트가 표시됩니다. e621 계정의 블랙리스트도 앱으로 가져올 수 있습니다.

* **데이터 없는 스마트 캐싱 & 화질 선택 (Smart Caching & Quality):** 한 번 로딩된 미디어는 MD5 해시로 기기에 자동 저장되어 오프라인에서도 감상할 수 있습니다. 이미지·동영상 화질을 각각 **원본/높음/중간/낮음** 중에서 고를 수 있고, **데이터 절약 모드**를 켜면 모바일 데이터에서 자동재생을 끄고 화질을 낮추며 일괄 다운로드를 막습니다. 구형 SWF 플래시 파일은 전용 네온 UI로 대체하여 렌더링합니다.

* **강력한 다운로드 (Powerful Downloads):**
  * **이어받기:** 네트워크가 끊겨도 HTTP Range로 이어서 받으며, 화면에 로딩된 미디어는 데이터 소모 없이 즉시 저장됩니다.
  * **포스트 일괄 다운로드:** 현재 검색 결과를 최대 320개까지 대기열로 내려받고 알림으로 진행률을 보여줍니다.
  * **파일명 규칙:** `{artist}_{id}`, `{date}_{id}` 등 원하는 형식으로 파일 이름을 지정할 수 있습니다.

* **편리한 환경 설정 & 관리 (Management & Updates):** 비디오 자동재생, 실시간 캐시 용량 관리, 다크 테마를 설정할 수 있습니다. 앱 설정과 팔로우 태그, 저장된 검색, 로컬 컬렉션은 Base64로 인코딩한 `.json` 파일로 백업 및 복원할 수 있습니다. GitHub 최신 릴리즈를 자동 감지하며, **앱 안에서 바로 APK를 내려받아 설치**할 수 있습니다.

* **Cloudflare 403 우회 & 무한 스크롤:** 403 차단 감지 시 캡차를 풀고 쿠키(`cf_clearance`)를 받아 이미지 썸네일과 비디오를 포함한 모든 통신 모듈에 즉시 적용합니다. e621의 요청 제한(503)은 캡차와 구분하여 자동으로 재시도합니다. 무한 스크롤은 중복 포스트를 차단하며, R-18 모드와 블랙리스트(기피 태그) 차단을 제공합니다.

<!-- -->

* **Modern UI & Dynamic Gestures:** Features a sleek Bento Grid-style detail screen, a floating search bar, and thumb-friendly Bottom Sheets. Supports real-time grid resizing (1 to 5 columns) via Pinch-to-Zoom, plus a **two-pane layout on tablets and in landscape** that shows the gallery and detail view side by side.

* **Pro-level UX:** Optimistic UI and Skeleton Shimmer loading deliver zero-delay interactions. Long-press a thumbnail to **select multiple posts** and batch download them or add them to a collection.

* **Flawless Deep Navigation:** Dive infinitely into tags; hitting back restores your exact scroll position and previously viewed posts.

* **Powerful In-App VPN:** Generates an encrypted bypass tunnel directly within the app without needing external VPN apps. (VPN configs and private keys are encrypted with the Android KeyStore.)
  * **Mullvad VPN Auto Connect:** Enter your 16-digit paid account number for a quick automatic connection to a JP server.
  * **WireGuard `.conf` Upload:** Supports uploading personal config files from services like Proton VPN.
  * **VPN Kill Switch:** Blocks e621 traffic while the in-app VPN is down to prevent exposing your real IP.
  * **Auto-Reconnect:** Automatically reconnects if the VPN drops unexpectedly.

* **Security & Privacy:**
  * **App Lock:** Lock the app with a 4-digit PIN and biometrics.
  * **Screen Protection:** Blocks screenshots and recent-apps previews.
  * **Incognito Mode:** Keeps no search or view history and wipes the cache when the app closes.
  * **Private Vault:** Keeps downloads inside the app instead of the gallery, with optional export to the gallery.

* **Search & Discovery:**
  * **Search History & Filter Chips:** Recent searches plus sort (top score, most favorited, random, oldest) and rating chips right in the search bar.
  * **Saved Searches:** Name your favorite queries and open them instantly as 📌 chips.
  * **Followed-Tag Notifications:** Get notified when followed tags receive new posts.
  * **Find Similar Images:** Find similar posts using e621's reverse image search (IQDB).
  * **Tag Info:** Long-press a tag to see its wiki and post count, then follow or blacklist it on the spot.
  * **View History & Local Collections:** Revisit recently viewed posts and organize posts into on-device folders without logging in.

* **Media Viewer & Ecosystem Integration:** Read user comments, navigate album Pools, and explore Parent/Child related posts with a single click. Pool Reader Mode lets you read comic-style content vertically in its original order, and the built-in WebM/MP4 ExoPlayer supports double-tap 10-second seeking.

* **Votes & Favorites:** Upvote (👍) or downvote (👎) posts from the detail view. **Upvoting automatically adds the post to your e621 favorites**, marked with a heart on its thumbnail. You can also import your e621 account blacklist.

* **Smart Caching & Quality Selection:** Loaded media is automatically saved via MD5 for offline viewing. Choose image and video quality separately (**Original / High / Medium / Low**), and enable **Data Saver** to disable autoplay, lower quality, and block batch downloads on mobile data. Unsupported legacy SWF files fall back to a custom neon warning UI.

* **Powerful Downloads:**
  * **Resumable:** Continues interrupted downloads via HTTP Range; cached media saves instantly without extra data.
  * **Batch Download Posts:** Queue up to 320 posts from the current search with progress notifications.
  * **Filename Templates:** Name files your way, e.g. `{artist}_{id}` or `{date}_{id}`.

* **Management & Updates:** Toggle video autoplay, monitor/clear cache size, and switch themes. Back up and restore your settings, followed tags, saved searches, and local collections via Base64-encoded `.json` files. Auto-detects the latest GitHub release and **downloads and installs the APK right inside the app**.

* **Cloudflare 403 Bypass & Infinite Scroll:** Detects 403 blocks, solves the captcha, and applies the `cf_clearance` cookie to all network layers (including ExoPlayer and Coil). e621 rate limits (503) are told apart from captchas and retried automatically. The infinite scroll prevents duplicate posts and supports an R-18 mode toggle and personalized blacklist blocking.

## 기술 스택 (Tech Stack)

* **UI:** Jetpack Compose, Material Design 3 (Pager, ModalBottomSheet, Adaptive Two-Pane Layout)
* **Network:** Retrofit2, OkHttp3, Paging 3, Coil (GIF/WebP support, Cloudflare bypass injection)
* **Media:** AndroidX Media3 (ExoPlayer)
* **Security:** Android KeyStore (AES/GCM Encryption) + Jetpack DataStore, AndroidX Biometric
* **Background:** WorkManager (followed-tag checks), FileProvider (in-app updates)
* **VPN Core:** `wireguard-android` (wireguard-go JNI wrapper), Android VpnService, Mullvad API

## 설치 및 사용 방법 (How to Use)

1. Release 탭에서 최신 버전의 `.apk` 파일을 다운로드하여 안드로이드 기기에 설치합니다. 이후 새 버전이 나오면 앱 안에서 바로 내려받아 설치할 수 있습니다. (처음 한 번은 '알 수 없는 앱 설치' 권한 허용이 필요합니다.)

2. 앱 첫 실행 시 나타나는 초기 화면에서 원하는 우회 방식을 선택합니다.
   * **Mullvad VPN:** 결제된 16자리 계정 번호를 입력하여 간편하게 연결합니다.
   * **WireGuard conf:** 본인의 설정 파일(`.conf`)을 선택하여 등록합니다.
   *(이미 외부 VPN을 사용 중이라면 '설정 안함'을 눌러 통과할 수 있습니다.)*

3. '연결하기' 또는 '터널 활성화' 버튼을 눌러 우회 터널을 뚫고 앱을 사용합니다.
상단 알약 모양의 검색바 우측 열쇠(VPN) 모양 아이콘을 통해 언제든 **VPN 설정 / VPN 해제**가 가능합니다.

4. **e621 로그인:** 추천·즐겨찾기, 블랙리스트 동기화 등을 사용하려면 상단 메뉴(햄버거 아이콘) -> `계정 로그인`에서 e621 Username과 API Key를 입력하는 것을 권장합니다.

5. **추천 설정:** 상단 메뉴 -> `앱 설정`에서 VPN 킬스위치, 앱 잠금, 화질, 데이터 절약 모드, 파일명 규칙, 앱 내 보관함 저장 등을 원하는 대로 설정하세요.

<!-- -->

1. Download the latest `.apk` file from the Release tab and install it. Future updates can be downloaded and installed right inside the app. (Allowing "Install unknown apps" is required the first time.)

2. Select your preferred VPN bypass method on the initial setup screen:
   * **Mullvad VPN:** Enter your valid 16-digit paid account number for a quick connection.
   * **WireGuard conf:** Register your own configuration file (`.conf`).
   *(If using an external VPN, tap 'Skip Setup'.)*

3. Tap 'Connect' or 'Enable Tunnel' to establish the bypass connection.
You can easily toggle the VPN anytime via the key icon on the top floating bar.

4. **e621 Login:** To use votes, favorites, and blacklist sync, it is highly recommended to enter your e621 Username and API Key via the top-left menu -> `Account Login`.

5. **Recommended Settings:** Visit the top-left menu -> `App Settings` to configure the VPN kill switch, app lock, quality, data saver, filename template, private vault, and more.

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
