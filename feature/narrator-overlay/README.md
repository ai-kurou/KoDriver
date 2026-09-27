# narrator-overlay

TelemetryLog に記録された最新の読み上げ内容をライブ購読し、オーバーレイ表示する機能。

購読には `ObserveLatestNarratedTelemetryLogUseCase` を使い、**実際に読み上げられたログだけ**を対象にする。
`NarrationOutcome.SKIPPED`（読み上げ条件は整ったが、キュー再生が無効で優先度に負けて読み上げされなかった）の
ログは音が鳴っていないため除外する。除外しないと、優先度で勝った項目が再生されている最中に、負けて
読み上げられなかった項目がオーバーレイに表示され、音と表示が食い違う。

`NarratorOverlayContent` が薄い灰色の背景に黄色系の文字で読み上げ内容を表示する。長文は折り返し、
収まらない場合は縦スクロールする。この画面はデスクトップアプリ（`app:desktopApp`、Windows / macOS / Linux
共通）が常時最前面・タスクバー非表示の専用ウィンドウにホストする（`app:shared` の `NarratorOverlayScreen`
経由）。ウィンドウのドラッグ移動・リサイズ、初期表示位置・サイズについては
`app/desktopApp/src/main/kotlin/kurou/kodriver/NarratorOverlayWindow.kt` を参照。
表示ON/OFFはその他タブの「オーバーレイ設定」→「オーバーレイを表示」で切り替える。設定は
`OverlayVisiblePreferencesRepository`（`:core:data`）に永続化し、`rememberNarratorOverlayVisible`
が購読して `NarratorOverlayWindowHost`（`app:desktopApp`）がウィンドウ自体の開閉に使う。
ウィンドウの位置・サイズも `OverlayWindowBoundsPreferencesRepository`（`:core:data`）へ永続化し、
次回起動時に復元する。ドラッグ・リサイズの直後ではなく 500ms のデバウンスを挟んで保存し、保存時と
モニタ構成が変わって画面外になる場合は既定位置（画面上部中央）にフォールバックする
（`rememberNarratorOverlayBounds` / `rememberNarratorOverlayBoundsSaver` と、`app:desktopApp` の
`NarratorOverlayWindowHost` / `OverlayWindowBoundsScreenCheck.kt` を参照）。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-narrator-overlay.svg)
<!-- MODULE-GRAPH-END -->
