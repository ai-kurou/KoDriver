# desktop-splash

デスクトップアプリ起動中に表示するスプラッシュ画面の状態管理を担うモジュール。

JVM の起動から Compose 初期化・Koin モジュール構築・Ktor サーバー起動までには数秒かかるため、その間の初期化フェーズ（[DesktopSplashStep]）を保持・購読するためのプラットフォーム非依存のコアを提供する。

- `DesktopSplashStep` … 初期化フェーズの定義と表示名。
- `DesktopSplashUiState` … スプラッシュ画面の表示状態（現在フェーズ・進捗率）。
- `DesktopSplashProgress` … main.kt の起動処理から駆動される進捗状態ホルダー。
- `desktopSplashModule` … `DesktopSplashProgress` を提供する Koin モジュール。

Composable UI と main.kt の起動フローへの配線は本モジュールには含まず、後続で実装する。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-desktop-splash.svg)
<!-- MODULE-GRAPH-END -->
