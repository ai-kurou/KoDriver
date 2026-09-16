# narrator-overlay

TelemetryLog に記録された最新の読み上げ内容をライブ購読し、オーバーレイ表示する機能。

`NarratorOverlayContent` が薄い灰色の背景に黄色系の文字で読み上げ内容を表示する。長文は折り返し、
収まらない場合は縦スクロールする。この画面は Windows 版デスクトップアプリ（`app:desktopApp`）が
常時最前面・タスクバー非表示の専用ウィンドウにホストする（`app:shared` の `NarratorOverlayScreen`
経由）。ウィンドウのドラッグ移動・リサイズ、初期表示位置・サイズについては
`app/desktopApp/src/main/kotlin/kurou/kodriver/NarratorOverlayWindow.kt` を参照。
位置・サイズの永続化、表示ON/OFFの設定は未実装（別PRで対応予定）。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-narrator-overlay.svg)
<!-- MODULE-GRAPH-END -->
