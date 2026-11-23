# splash

スプラッシュ画面を担うモジュール。パッケージで共通部分とデスクトップ専用部分に分けている。

## 共通（`kurou.kodriver.feature.splash`）

Android とデスクトップで同じ演出にするための部品。

- `SplashBackdrop` … 自己ベストラップの背景画像（`ReadoutBackground.BEST_LAP`）を右側に置いて左へ溶かし、その上に光の帯を重ねる背景。光の位置は `shineProgress`（0〜1）で渡す。
- `SplashAppIcon` … アプリアイコン。

## Android 専用（`kurou.kodriver.feature.splash.android`）

OS のスプラッシュ（`installSplashScreen`）が終わった直後に、メイン画面の上へ約1秒だけ重ねる演出。

- `AndroidSplashOverlay` … `SplashBackdrop` とアイコンを重ね、光が横切ったあとフェードアウトして `onFinished` を一度通知する。表示中はタッチを消費し、背後のメイン画面を操作させない。縦横は `BoxWithConstraints` の描画領域で決め、縦長のときは写真を下端に置く。
- `shouldShowAndroidSplashOverlay` … 表示の可否。コールドスタート（保存状態からの復元でなく、このプロセスで未表示）で、端末のアニメーション縮尺（`ANIMATOR_DURATION_SCALE`）が 0 でないときだけ表示する。

`:app:shared` の `AndroidSplashHost` が表示の可否を判断し、`MainActivity` が判定に使う値を渡す。演出は OS のスプラッシュが外れたことを `MainActivity` が伝えてから始める（メイン画面の初回描画後も OS のスプラッシュはしばらく残るため）。通知が届かないときは一定時間後に始める。オーバーレイはアプリのテーマ（ダークテーマ・動的カラー）に合わせるため、`AppScreen` の `overlayContent` として渡す。

## デスクトップ専用（`kurou.kodriver.feature.splash.desktop`）

JVM の起動から Compose 初期化・Koin モジュール構築・Ktor サーバー起動までには数秒かかるため、その間の初期化フェーズ（[DesktopSplashStep]）を保持・購読するためのプラットフォーム非依存のコアを提供する。

- `DesktopSplashStep` … 初期化フェーズの定義と表示名。
- `DesktopSplashUiState` … スプラッシュ画面の表示状態（現在フェーズ・進捗率）。
- `DesktopSplashProgress` … 起動処理から駆動される進捗状態ホルダー。
- `runInitialization` … 進捗を更新しながら初期化処理を順に実行する `DesktopSplashProgress` 拡張関数。
- `DesktopSplashScreen` … `SplashBackdrop` の上に、アプリアイコン・進捗バー・フェーズ名を左側に表示する Composable。起動完了（`READY`）で光が画面を横切り、終わると `onReadyAnimationFinished` を一度通知する。
- `DesktopSplashErrorDialog` … 初期化に失敗したことを通知するダイアログ。

進捗ホルダーは Koin 起動前に生成する必要があるため、DI モジュールは提供せず、`:app:shared` の `DesktopSplashHost` から `DesktopSplashProgress` を直接生成して駆動する。`:app:desktopApp` の `main.kt` は `DesktopSplashHost` に Koin 構築・サーバー起動処理を渡して配線する。

`DesktopSplashHost` は初期化完了後、この通知を受けるまでスプラッシュ画面を保ち、光を最後まで見せてからメイン画面へ切り替える。

初期化処理が例外をスローした場合、スプラッシュ画面が完了しないまま停止するのを避けるため、`DesktopSplashHost` は `DesktopSplashErrorDialog` を表示する。ダイアログを閉じると `onError` が通知され、`main.kt` は Sentry へ送信のうえアプリを終了する。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-splash.svg)
<!-- MODULE-GRAPH-END -->
