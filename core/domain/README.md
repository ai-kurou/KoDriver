# domain

## オーバーレイ文字サイズのプレビュー

`PreviewOverlayTextSizeUseCase` は `OverlayTextSizePreferencesRepository.setPreviewOverlayTextSize` に
一時値を渡す。`null` で解除し、`ObserveOverlayTextSizeUseCase` はプレビュー優先の現在値を配信する。
永続化は `SaveOverlayTextSizeUseCase` が担当する。

## オーバーレイ文字色のプレビュー

`PreviewOverlayTextColorUseCase` は `OverlayTextColorPreferencesRepository.setPreviewOverlayTextColor` に
一時値を渡す。`null` で解除し、`ObserveOverlayTextColorUseCase` はプレビュー優先の現在値を配信する。
永続化は `SaveOverlayTextColorUseCase` が担当する。

## オーバーレイ背景透明度のプレビュー

`PreviewOverlayBackgroundOpacityUseCase` は `OverlayBackgroundOpacityPreferencesRepository.setPreviewOverlayBackgroundOpacity` に
一時値を渡す。文字サイズと同様に `null` で解除し、`ObserveOverlayBackgroundOpacityUseCase` はプレビュー優先の現在値を配信する。
永続化は `SaveOverlayBackgroundOpacityUseCase` が担当する。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/core-domain.svg)
<!-- MODULE-GRAPH-END -->

## 自由文言の試聴

`ReadoutPreviewGuard` は、空白文言・TTS利用不可・音量0以下の再生抑止とTTS利用可否の取得を共通化する。
`ReadoutTextPreviewHelper` はこの判定を使用し、項目の開始音完了後にTTSを再生する。
GT7の燃料残り周回数・燃料残量・タイヤ温度・自己ベストラップ、LMUのタイヤ温度・バーチャルエナジー残量・車両接近・ピットタイミング・自己ベストラップ・フラッグ、ACEのフラッグ・自己ベストラップの詳細ViewModelで使用する。
文言のプレースホルダー置換とサンプル値は各ViewModelが決め、解決済み文言と項目キーを渡す。
利用可否はViewModelのスコープで一度取得し、初期値falseで保持する。試聴はsuspend関数を同じスコープから呼び出し、キャンセル・例外は呼び出し元へ伝播する。

`ReadoutSpeechEventPreviewHelper` は同じ判定を使用し、LMUのタイヤ摩耗・ブレーキ温度・車両損傷の詳細ViewModelから受け取ったイベントを `PlaySpeechEventUseCase` へ渡す。
文言の置換とイベント生成は各ViewModelが担当し、イベント経由の開始音・TTS再生を維持する。

## ACEフラッグの読み上げ文言設定

`AceWindowsFlagReadoutTextPreferencesRepository` は `AceWindowsFlagReadoutTextKey` を指定する
`observeText` / `saveText` で、設定可能な10種類のフラッグ文言を取得・保存する。
フラッグなし・不明は設定対象に含めない。フラッグ別のUseCaseは対応するキーを指定し、
保存時の空白除去・最大文字数制限を維持する。

## LMUピットタイミング設定

`LmuWindowsPitTimingPreferencesRepository` は予想残り周回数・読み上げ有効状態、
`LmuWindowsPitTimingReadoutTextPreferencesRepository` は予告・直前の読み上げ文言を扱う。
Factoryは両Repositoryの組を返し、Android／DesktopのDIで同じDataStoreを共有する。
既存の保存ファイル・フィールド・ProtoNumberを維持するため、設定移行は不要。

## LMUタイヤ温度設定

`LmuWindowsTyreTemperaturePreferencesRepository` は有効状態・低温警告対象フェーズ、
`LmuWindowsTyreTemperatureReadoutTextPreferencesRepository` は過熱・低温警告の文言を扱う。
Factoryが返す両RepositoryはAndroid／DesktopのDIで1つのDataStoreを共有する。
保存ファイルと現行フィールドのProtoNumberを維持するため、設定移行は不要。廃止したProtoNumber 1は再利用しない。

オーバーレイ文字色は ARGB の Long として保存し、`OVERLAY_TEXT_COLOR_DEFAULT`（`0xFFFFC107L`）を既定値とする。`ObserveOverlayTextColorUseCase` は保存値を配信し、`SaveOverlayTextColorUseCase` は選択したARGB値をRepositoryへ保存する。

## デバッグ読み上げ

`DebugReadoutRequest` と `DebugReadoutRequestRepository`、送信・購読用の `RequestDebugReadoutUseCase` / `ObserveDebugReadoutRequestUseCase` を提供する。`ReadoutItemKey.topLevelEntriesOf(simulator)` は一覧項目だけを entries 順に返す。`DebugReadoutText` は入力文言を置換せず音声経路へ渡すイベントで、本文の再解決には copy を使う。
