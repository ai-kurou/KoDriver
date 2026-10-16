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
フィールド・ProtoNumberは維持する。保存ファイル名の `auto_` 導入時は設定を意図的に初期化し、旧ファイルの移行は行わない。

## LMUタイヤ温度設定

`LmuWindowsTyreTemperaturePreferencesRepository` は有効状態・低温警告対象フェーズ、
`LmuWindowsTyreTemperatureReadoutTextPreferencesRepository` は過熱・低温警告の文言を扱う。
Factoryが返す両RepositoryはAndroid／DesktopのDIで1つのDataStoreを共有する。
現行フィールドのProtoNumberは維持する。保存ファイル名の `auto_` 導入時は設定を意図的に初期化し、旧ファイルの移行は行わない。廃止したProtoNumber 1は再利用しない。

オーバーレイ文字色は ARGB の Long として保存し、`OVERLAY_TEXT_COLOR_DEFAULT`（`0xFFFFC107L`）を既定値とする。`ObserveOverlayTextColorUseCase` は保存値を配信し、`SaveOverlayTextColorUseCase` は選択したARGB値をRepositoryへ保存する。

## デバッグ読み上げ

`DebugReadoutRequest` と `DebugReadoutRequestRepository`、送信・購読用の `RequestDebugReadoutUseCase` / `ObserveDebugReadoutRequestUseCase` を提供する。`ReadoutItemKey.topLevelEntriesOf(simulator)` は一覧項目だけを entries 順に返す。`DebugReadoutText` は入力文言を置換せず音声経路へ渡すイベントで、本文の再解決には copy を使う。

`HotkeyRepository` は登録状態と押下の Flow、競合後の再試行を提供する。キーの取得・保存は `ReadoutHotkeyPreferencesRepository` が担う。
永続化は `ReadoutHotkeyPreferencesRepository`、既定 F1 と固定修飾キーは `ReadoutHotkeyDefaults.kt` に定義する。
ホットキー設定と登録状態の UseCase はデバッグペインで使用し、`ObserveReadoutHotkeyUseCase` の押下通知は従来どおり Narrator で使用する。

## 手動読み上げセット

`ManualReadoutDefaults.kt` の `MANUAL_READOUT_SET_ITEM_MAX`（3）は、1セットに追加できる項目数の上限。詳細画面と ViewModel の追加処理で参照する。

`ManualReadoutSet` はセット識別子・読み上げ項目キーのリスト・項目キーごとの文言テンプレート `itemTexts` を持つ。
文言が未保存なら `manualReadoutItemTextDefault` の既定値を使い、保存済みの空文字は読み上げない。
LMU のバーチャルエナジー残量・タイヤ摩耗・ブレーキ残量は自動側と共通の `{percent}` 置換・未知プレースホルダー検出を使う。
`ObserveManualReadoutSetsUseCase` / `SaveManualReadoutSetsUseCase` は
`ManualReadoutSetPreferencesRepository` を通じてセット一覧を取得・保存する。
`AddManualReadoutSetUseCase` は保存済み一覧の末尾へ1セットをアトミックに追加する。
`ManualReadoutRequest(simulatorId, setId)` は Narrator にセットを読むよう依頼するモデル。
`RequestManualReadoutUseCase` / `ObserveManualReadoutRequestsUseCase` は
`ManualReadoutRequestRepository` を通じてアプリ内の依頼を送信・購読する。

`ManualReadoutSet.hotkeyModifiers` は `ManualReadoutHotkeyModifiers` の4種類から選択する。Windows の修飾キー値を保持し、既定値は `ReadoutHotkeyDefaults.kt` の `MANUAL_READOUT_HOTKEY_MODIFIERS_DEFAULT`（Ctrl + Alt + Shift）。不明値・null は既定値として扱う。

`ManualReadoutHotkeyRepository` はセットホットキーの押下（`ManualReadoutRequest`）とセット別の登録状態を配信し、競合・失敗時の再試行を受け付ける。対応する Observe / Retry UseCase を通じて Narrator とセット詳細画面から利用する。

手動タイヤ摩耗は4輪の最小残存率×100を四捨五入し、既定文言「タイヤ摩耗残り{percent}%」へ置換する。未受信・鮮度切れ・空の wheels はデータ無しとして扱う。自動警告の閾値・状態は変更しない。

LMU の手動セットは鮮度判定済みの残存率を `LmuWindowsManualReadoutItem` で受け取り、`determineManualReadout` 内で既存の `PERCENTAGE_SCALE` を使って整数%へ四捨五入する。各項目の文を保存順に「、」で連結し、`LmuWindowsManualReadoutText` の1イベントで読み上げる。項目のデータが無い場合は `manualReadoutItemName` の日本語名を使った「<項目名>はデータがありません」を連結する。全項目がデータ無しなら、1項目の場合も含め「データがありません」のみ。対象項目なしは「読み上げる項目がありません」を維持する。

手動ブレーキ残量は `BrakeWear.Root` に対応し、4輪の `remainingPercent`（0〜100）の最小値を四捨五入して、既定文言「ブレーキ残量{percent}%」へ置換する。セットの保存順に他項目と連結し、先頭項目の開始音と常時キュー追加を使う。未受信・受信から5秒超・空の wheels はデータ無しとして扱い、自動警告の前回値・警告済み状態は変更しない。
