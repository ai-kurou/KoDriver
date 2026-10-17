# domain

## パッケージ構成

シミュレーター固有コードの配置先は `kurou.kodriver.domain.<lmuwindows|acewindows|gt7ps5>.{model,repository,usecase}` とする。
共通コードは `kurou.kodriver.domain` 直下の `model` / `repository` / `usecase` / `engine` に置く。
GT7・ACE・LMU はすべて移動済み。
`ReadoutItemKey` のシミュレーター別サブタイプと `Gt7Ps5SpeechEvent`・`AceWindowsSpeechEvents`・`LmuWindowsSpeechEvents` は共通パッケージに残す。
sealed の直接サブタイプは同一パッケージ・同一モジュールに置く必要があるため。

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

`ManualReadoutSet` はセット識別子・セット名 `name`（空文字なら画面側で「セット N」を表示。上限は `MANUAL_READOUT_SET_NAME_MAX_LENGTH`）・読み上げ項目キーのリスト・項目キーごとの文言テンプレート `itemTexts` を持つ。
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

LMU の手動セットは鮮度判定済みの残存率・摂氏温度を `LmuWindowsManualReadoutItem` で受け取り、`determineManualReadout` 内で残存率は既存の `PERCENTAGE_SCALE` を使って整数%へ、温度は整数℃へ四捨五入する。各項目の文を保存順に「、」で連結し、`LmuWindowsManualReadoutText` の1イベントで読み上げる。項目のデータが無い場合は `manualReadoutItemName` の日本語名を使った「<項目名>はデータがありません」を連結する。全項目がデータ無しなら、1項目の場合も含め「データがありません」のみ。対象項目なしは「読み上げる項目がありません」を維持する。

手動ブレーキ残量は `BrakeWear.Root` に対応し、4輪の `remainingPercent`（0〜100）の最小値を四捨五入して、既定文言「ブレーキ残量{percent}%」へ置換する。セットの保存順に他項目と連結し、先頭項目の開始音と常時キュー追加を使う。未受信・受信から5秒超・空の wheels はデータ無しとして扱い、自動警告の前回値・警告済み状態は変更しない。

手動ブレーキ温度は `BrakeTemperature.Root` に対応し、`LmuWindowsBrakeTemperatureData.wheels` の最高温度を四捨五入した整数℃を、既定文言「ブレーキ温度{celsius}℃」へ置換する。未受信・受信から5秒超・空の wheels はデータ無しとして扱う。セットの保存順に他項目と連結し、先頭項目の開始音と常時キュー追加を使う。自動過熱警告の前回値・警告済み状態は変更しない。

手動タイヤ温度は `TyreTemperature.Root` に対応し、`LmuWindowsTyreCarcassTemperatureData.wheels` の `CelsiusReading.value` の最大カーカス温度を四捨五入した整数℃を、既定文言「タイヤ温度{celsius}℃」へ置換する。未受信・受信から5秒超・空の wheels はデータ無しとして扱う。セットの保存順に他項目と連結し、先頭項目の開始音と常時キュー追加を使う。自動過熱・低温警告の前回値・警告済み状態は変更しない。

`LmuWindowsManualReadoutItem` は従来の `ratio`（0〜1）に加え、既定値 null の `celsius: Float?` を保持する。温度項目では `celsius`、割合項目では `ratio` の有無でデータ無しを判定する。温度の整数化と `{celsius}` 置換も `determineManualReadout` で行い、既存の連結イベントを使う。

GT7 の手動読み上げは、一覧の ▶ と Windows のセットホットキーからの依頼を合流し、
`Simulator.Gt7Ps5.id` 宛てで保存済みセットが見つかり、GT7 が選択中の場合だけ処理する。
`manualReadoutItemKeys(Simulator.Gt7Ps5.id)` は「残り燃料」の
`Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value`、続いて「残り燃料の周回数」の
`Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value` に対応する。詳細画面で保存したセットの
`itemKeys`・`itemTexts` を ViewModel が参照し、`Gt7Ps5ManualReadoutItem` を判定へ渡す。
最新テレメトリの `gasLevel / gasCapacity * 100` を四捨五入して0〜100に制限する。
未受信・受信から5秒超・不正な燃料値は null とし「残り燃料はデータがありません」を読む。
既定文言は `GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT` の
「燃料は残り{percent}パーセント」で、既存の燃料用フォーマット関数を再利用する。
判定は対応項目を最大3件、保存順に空欄を除いて「、」で連結し、
`Gt7Ps5ManualReadoutText` の1イベントを返す。現在値イベントは `Gt7Ps5RemainingFuelCurrentValue` と `Gt7Ps5RemainingFuelLapsCurrentValue`。
対応項目なしの場合は従来どおり「読み上げる項目がありません」を読む。
自動読み上げのスイッチ・警告済み状態は変更せず、常にキュー末尾へ追加して同じ本文をログに保存する。
開始音とログには、項目なしの場合も既存の `RemainingFuel.Root` を使う。
GT7 と LMU のキーはそれぞれ `gt7_ps5_`・`lmu_windows_` 接頭辞を持ち衝突しない。
手動項目の選択・文言はシミュレーター別のセット設定に保存し、自動項目のスイッチとは独立して判定する。


手動の残り燃料周回数は `currentRemainingFuelLaps(state)` で `fuelTrackingState` から算出する。
開始燃料＋総給油量－現在燃料を、完走周回数＋`CURRENT_LAP_CONSUMPTION_WEIGHT`（0.9）で割った平均消費を使い、
現在燃料÷平均消費を整数へ切り捨てる。開始燃料・開始周回が未設定、完走周回数が0以下、消費燃料が0以下なら null。
最終受信から5秒以内だけ推定値を使い、未受信・鮮度切れは「残り燃料の周回数はデータがありません」を読む。
`Gt7Ps5ManualReadoutItem.value` の単位は残り燃料が％、残り燃料の周回数が周。
既定文言は `GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT`（「燃料は残り約{laps}周」）で、
`formatGt7Ps5RemainingFuelLapsReadoutText` で置換する。0周以下は「燃料残り1周未満」。
テンプレートが空欄なら、0周以下やデータなしでも読まない。
自動側のしきい値・重複抑止・周回内タイミング・有効スイッチは手動側に適用しない。
テレメトリごとに `determineRemainingFuelLaps` が呼ばれ、有効スイッチの判定より前に燃料追跡が更新されるため、
自動読み上げが無効でも手動用の追跡が続く。
