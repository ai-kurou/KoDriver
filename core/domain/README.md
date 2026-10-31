# domain

`ResetAutoReadoutSettingsUseCase` は受け取った `AutoReadoutSettingsResetter` を順番に実行し、全シミュレーターの自動読み上げ設定を一括で既定値に戻します。途中で失敗した場合は例外を呼び出し側に伝播します。
`ResetManualReadoutSettingsUseCase` も同形で、受け取った `ManualReadoutSettingsResetter` を順番に実行し、手動読み上げセット（ホットキーを含む）と再生ボタン表示設定を既定値に戻します。

## パッケージ構成

シミュレーター固有コードの配置先は `kurou.kodriver.domain.<lmuwindows|acewindows|gt7ps5>.{model,repository,usecase}` とする。
共通コードは `kurou.kodriver.domain` 直下の `model` / `repository` / `usecase` / `engine` に置く。
GT7・ACE・LMU はすべて移動済み。
`:core:domain` は共通 `ReadoutItemKey` インターフェースと `ReadoutItemKeyRegistry` を提供する。シミュレーター別の sealed key、キー一覧 `*ReadoutItemKeys.kt`、および `*ReadoutDefaults.kt` は各 simulator domain に置く。キーを追加する際は、そのモジュールの `*ReadoutItemKeys.kt` にも登録する。`SpeechEvent` はシミュレーター別ドメインモジュールで実装し、手動読み上げ項目定義は各シミュレーターのドメインモジュール、simulatorId 集約は `:feature:manual-readout-detail` に置く。

`kurou.kodriver.domain.util` の `isManualReadoutHotkeySupported()`（expect/actual）は手動読み上げのホットキー対応判定で、Windows デスクトップのみ true を返す。`:feature:manual-readout-list` と `:feature:manual-readout-detail` が共用する。

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

手動の「燃料の残り周回数」は `LmuWindowsReadoutItemKey.RemainingFuel.Laps.value`（`lmu_windows_auto_remaining_fuel_laps`）に対応する。
`RemainingFuel` 配下の手動専用項目で、TopLevel・自動タブ・スイッチには追加しない。手動項目定義の末尾（タイヤ摩耗の残り周回数の後）に並ぶ。
`fuelLapsTrackingState` の蓄積値を `manualFuelRemainingLaps` で読み、1周以上は `formatLmuWindowsPitTimingReadoutText` で `{laps}` を置換する。
既定文言は `LMU_WINDOWS_MANUAL_READOUT_FUEL_LAPS_TEXT_DEFAULT`（「燃料は残り約{laps}周」）、0周は
`LMU_WINDOWS_MANUAL_READOUT_FUEL_LAPS_EMPTY_TEXT_DEFAULT`（「燃料残り1周未満」）。空欄テンプレートなら0周でも空文字にする。
蓄積前・消費量が null／0以下／非有限・算出不能・最新燃料が未受信または受信から5秒超の場合はデータ無しとする。
鮮度は `latestFuelReceivedAtMs` で判定し、ピットタイミングの鮮度には依存しない。
複数項目では「燃料の残り周回数はデータがありません」を連結する。
先頭項目なら開始音・優先度・ログ・キューに `RemainingFuel.Root` を使う。編集画面はACE・GT7の燃料項目と同じ「エネルギー」カテゴリに配置し、既存の
`LMU_WINDOWS_PIT_TIMING_LAPS_PLACEHOLDER` を挿入・検証に使う。項目一覧・ホットキーは共通の手動項目定義を参照する。
手動の燃料読み上げは自動警告の有効状態・評価履歴を変更しない。

手動の「クラス内前方車／後方車とのギャップ」は `LmuWindowsReadoutItemKey.ClassCarGap.Ahead.value`（`lmu_windows_auto_class_car_gap_ahead`）・`ClassCarGap.Behind.value`（`lmu_windows_auto_class_car_gap_behind`）に対応する。
TopLevel・自動タブ・スイッチ・`LmuWindowsReadoutDefaults.kt` の列挙には追加しない手動専用項目で、`LMU_WINDOWS_READOUT_ITEM_KEYS` の末尾（`MyBestLap.DetailEnabled` の後）と手動項目定義の末尾（燃料の残り周回数の後）に並ぶ。
`ObserveLmuWindowsClassCarGapUseCase` の最新値（`LmuWindowsClassCarGapData`）を `latestClassCarGap` / `latestClassCarGapReceivedAtMs` に保持し、受信から5秒（`CURRENT_VALUE_MAX_AGE_MS`）以内のものだけを項目へ渡す。未受信・鮮度切れはデータ無しとする。
周回遅れ・周回先行の車両も前後の対象に含む。トップ・最後尾は `gapAheadSeconds` / `gapBehindSeconds` ではなく `classPosition == 1` / `classPosition == classVehicleCount`（両方非 null）で判定し、固定文言
`LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TOP_TEXT_DEFAULT`（「クラス内先頭です」）・`LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_LAST_TEXT_DEFAULT`（「クラス内最後尾です」）を読む（データありとして扱う。空欄テンプレートなら空文字）。
それ以外は null・非有限・負値の秒差をデータ無しとし、0以上の有限値を `formatLmuWindowsClassCarGapReadoutText` で小数1桁に丸めて `{seconds}`（`LMU_WINDOWS_CLASS_CAR_GAP_SECONDS_PLACEHOLDER`）を置換する。
既定文言は `LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_AHEAD_TEXT_DEFAULT`（「クラス内前方{seconds}秒」）・`..._BEHIND_TEXT_DEFAULT`（「クラス内後方{seconds}秒」）。
複数項目ではデータ無しの項目に「<項目名>はデータがありません」を連結する。
先頭項目なら開始音・優先度・ログ・キューに `VehicleApproach.Root` を使う。編集画面は既存の「ラップ」カテゴリに配置し、`{seconds}` を挿入・検証に使う。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/core-domain.svg)
<!-- MODULE-GRAPH-END -->

## 自由文言の試聴

`ReadoutPreviewGuard` は、空白文言・TTS利用不可・音量0以下の再生抑止とTTS利用可否の取得を共通化する。
`ReadoutTextPreviewHelper` はこの判定を使用し、項目の開始音完了後にTTSを再生する。
GT7の燃料残り周回数・燃料残量・タイヤ温度・自己ベストラップ、LMUのタイヤ温度・バーチャルエネルギー残量・車両接近・ピットタイミング・自己ベストラップ・フラッグ、ACEのフラッグ・自己ベストラップの詳細ViewModelで使用する。
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
手動読み上げの残り周回数推定は、商が1,000周を超える場合をデータなしとして扱う。ただし上限との比較では微小な浮動小数点誤差を許容し、読み上げ値は1,000周に収める。

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
LMU のバーチャルエネルギー残量・燃料残量・タイヤ摩耗・ブレーキ残量は自動側と共通の `{percent}` 置換・未知プレースホルダー検出を使う。
`ObserveManualReadoutSetsUseCase` / `SaveManualReadoutSetsUseCase` は
`ManualReadoutSetPreferencesRepository` を通じてセット一覧を取得・保存する。
`AddManualReadoutSetUseCase` は保存済み一覧の末尾へ1セットをアトミックに追加する。
`ManualReadoutRequest(simulatorId, setId)` は Narrator にセットを読むよう依頼するモデル。
`RequestManualReadoutUseCase` / `ObserveManualReadoutRequestsUseCase` は
`ManualReadoutRequestRepository` を通じてアプリ内の依頼を送信・購読する。

`ManualReadoutSet.hotkeyModifiers` は `ManualReadoutHotkeyModifiers` の3種類から選択する。Windows の修飾キー値を保持し、既定値は `ReadoutHotkeyDefaults.kt` の `MANUAL_READOUT_HOTKEY_MODIFIERS_DEFAULT`（Ctrl + Alt）。不明値・null は既定値として扱う。

`ManualReadoutHotkeyRepository` はセットホットキーの押下（`ManualReadoutRequest`）とセット別の登録状態を配信し、競合・失敗時の再試行を受け付ける。対応する Observe / Retry UseCase を通じて Narrator とセット詳細画面から利用する。

手動タイヤ摩耗は4輪の最小残存率×100を四捨五入し、既定文言「タイヤ摩耗残り{percent}%」へ置換する。未受信・鮮度切れ・空の wheels はデータ無しとして扱う。自動警告の閾値・状態は変更しない。

LMU の手動セットは鮮度判定済みの残存率・摂氏温度・ラップタイム・バーチャルエネルギーの残り周回数を `LmuWindowsManualReadoutItem` で受け取り、`determineManualReadout` 内で残存率は既存の `PERCENTAGE_SCALE` を使って整数%へ、温度は整数℃へ四捨五入する。各項目の文を保存順に「、」で連結し、`LmuWindowsManualReadoutText` の1イベントで読み上げる。項目のデータが無い場合は `manualReadoutItemName` の日本語名を使った「<項目名>はデータがありません」を連結する。保存データが3件を超えても先頭 `MANUAL_READOUT_SET_ITEM_MAX`（3件）までを読む。対象項目なしは「読み上げる項目がありません」を維持する。

LMU の手動「燃料残量」は既存の `LmuWindowsReadoutItemKey.RemainingFuel.Root` を使う。受信から5秒以内の `remainingPercent` を比率（0〜1）に変換し、手動判定で四捨五入した整数％を、既定文言 `LMU_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT`（「燃料は残り{percent}パーセント」）へ埋める。未受信・鮮度切れは「燃料残量はデータがありません」を連結する。先頭が燃料残量なら開始音・優先度・ログのキーは `RemainingFuel.Root`。自動燃料警告の `remainingFuelWarned` は変更しない。

手動ブレーキ残量は `BrakeWear.Root` に対応し、4輪の `remainingPercent`（0〜100）の最小値を四捨五入して、既定文言「ブレーキ残量{percent}%」へ置換する。セットの保存順に他項目と連結し、先頭項目の開始音と常時キュー追加を使う。未受信・受信から5秒超・空の wheels はデータ無しとして扱い、自動警告の前回値・警告済み状態は変更しない。

手動ブレーキ温度は `BrakeTemperature.Root` に対応し、`LmuWindowsBrakeTemperatureData.wheels` の最高温度を四捨五入した整数℃を、既定文言「ブレーキ温度{celsius}℃」へ置換する。未受信・受信から5秒超・空の wheels はデータ無しとして扱う。セットの保存順に他項目と連結し、先頭項目の開始音と常時キュー追加を使う。自動過熱警告の前回値・警告済み状態は変更しない。

手動タイヤ温度は `TyreTemperature.Root` に対応し、`LmuWindowsTyreCarcassTemperatureData.wheels` の `CelsiusReading.value` の最大カーカス温度を四捨五入した整数℃を、既定文言「タイヤ温度{celsius}℃」へ置換する。未受信・受信から5秒超・空の wheels はデータ無しとして扱う。セットの保存順に他項目と連結し、先頭項目の開始音と常時キュー追加を使う。自動過熱・低温警告の前回値・警告済み状態は変更しない。

手動自己ベストラップは `LmuWindowsReadoutItemKey.MyBestLap.Root.value` に対応し、セッションの `timing.bestLapTimeMs` を読む。既定文言は `LMU_WINDOWS_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT`（「自己ベストラップ {laptime}」）。既存の `formatLmuWindowsMyBestLapReadoutText` で `{laptime}` を「1分23秒456」形式に置換する。未受信・最後の受信から `CURRENT_VALUE_MAX_AGE_MS`（5秒）超・0以下はデータ無しとし、複数項目では「自己ベストラップはデータがありません」を連結。自動の `MyBestLap.Root` / `DetailEnabled` スイッチとは独立し、`determineMyBestLap`・`personalBestMs` の自動更新判定の状態は変更しない。保存順に連結した1イベントを常にキュー末尾へ追加し、開始音・優先度・ログには先頭項目のキーを使う。

手動エネルギー残り周回数は `LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value` に対応し、蓄積済みの `pitTimingVirtualEnergyTrackingState` から `(currentValue / lastValidLapConsumption).toInt()` で算出する。既定文言は `LMU_WINDOWS_MANUAL_READOUT_VIRTUAL_ENERGY_LAPS_TEXT_DEFAULT`（「エネルギーは残り約{laps}周」）で、1周以上なら `formatLmuWindowsPitTimingReadoutText` で置換し、0周なら固定文言「エネルギー残り1周未満」を読む。蓄積前（`currentLap == -1`）、消費量が null・0以下、算出結果が負、最新エネルギーが未受信・受信から5秒超の場合はデータ無しとし、複数項目では「バーチャルエネルギーの残り周回数はデータがありません」を連結。自動のスイッチ・閾値・ベストラップ経過時間には依存せず、追跡状態・自動判定状態（`lastAnnouncedPitTimingVirtualEnergyLaps` 等）は変更しない。保存順の1イベントを常にキュー末尾へ追加し、先頭がこの項目なら開始音・優先度・ログには `PitTiming.Root` を使う。タイヤ摩耗の残り周回数は別項目として扱う。

手動タイヤ摩耗残り周回数は `LmuWindowsReadoutItemKey.PitTiming.TyreWear.value`（項目名「タイヤ摩耗の残り周回数」）に対応し、蓄積済みの `pitTimingTyreWearTrackingState` からエネルギーと同じ式で算出する（共通の private ヘルパーを `manualPitTimingTyreWearRemainingLaps` から呼ぶ）。既定文言は `LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_LAPS_TEXT_DEFAULT`（「タイヤは残り約{laps}周」）で、1周以上なら `formatLmuWindowsPitTimingReadoutText` で置換し、0周なら固定文言「タイヤ残り1周未満」を読む（空欄テンプレートなら0周でも空文字）。蓄積前・消費量が null・0以下・非有限、算出結果が負、最新タイヤ摩耗が未受信・受信から5秒超の場合はデータ無しとし、`PitTiming.Root` を開始音・優先度・ログに使う。バーチャルエネルギーの残り周回数とは別項目で、既存の「タイヤ摩耗」(%) 項目とも別。

`LmuWindowsManualReadoutItem` は従来の `ratio`（0〜1）に加え、既定値 null の `celsius: Float?` ・`lapTimeMs: Long?`・`remainingLaps: Int?` を保持する。エネルギー周回数では0以上の `remainingLaps`、自己ベストでは正の `lapTimeMs`、温度項目では `celsius`、割合項目では `ratio` の有無でデータ無しを判定する。温度の整数化と `{celsius}` 置換も `determineManualReadout` で行い、既存の連結イベントを使う。

GT7 の手動読み上げは、一覧の ▶ と Windows のセットホットキーからの依頼を合流し、
`Simulator.Gt7Ps5.id` 宛てで保存済みセットが見つかり、GT7 が選択中の場合だけ処理する。
`manualReadoutItemKeys(Simulator.Gt7Ps5.id)` は「残り燃料」の
`Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value`、続いて「残り燃料の周回数」の
`Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value`、続いて「タイヤ温度」の
`Gt7Ps5ReadoutItemKey.TyreTemperature.Root.value` に対応する。詳細画面で保存したセットの
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
`Gt7Ps5ManualReadoutItem.value` の単位は残り燃料が％、残り燃料の周回数が周、タイヤ温度が℃。
既定文言は `GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT`（「燃料は残り約{laps}周」）で、
`formatGt7Ps5RemainingFuelLapsReadoutText` で置換する。0周以下は「燃料残り1周未満」。
テンプレートが空欄なら、0周以下やデータなしでも読まない。
自動側のしきい値・重複抑止・周回内タイミング・有効スイッチは手動側に適用しない。
テレメトリごとに `determineRemainingFuelLaps` が呼ばれ、有効スイッチの判定より前に燃料追跡が更新されるため、
自動読み上げが無効でも手動用の追跡が続く。

GT7 の手動「タイヤ温度」は `TyreTemperature.Root` を使い、`currentMaxTyreTemperatureCelsius(telemetry)` で
4輪の有限値の最高温度を `roundToInt()` した整数℃を取得する。未受信・5秒超・全輪が非有限値または0（取得不能）は
「タイヤ温度はデータがありません」。既定文言は `GT7_PS5_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT`
（「タイヤ温度{celsius}℃」）で、既存の GT7 用フォーマッターとプレースホルダー検証を使う。
詳細のカテゴリはタイヤ、プレビューは90℃。空文言は読まず、自動のしきい値・ヒステリシス・重複抑止・
スイッチ判定は適用しない。最大3項目を「、」で連結し、開始音・ログのキーは `RemainingFuel.Root` を維持する。

GT7 の手動「自己ベストラップ」は `MyBestLap.Root` を使い、`currentBestLapTimeMs(telemetry)` でテレメトリの
`bestLapTimeMs`（ms）を取得する。未受信・5秒超・0以下（未確定）は「自己ベストラップはデータがありません」。
既定文言は `GT7_PS5_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT`（「自己ベストラップ {laptime}」）で、
既存の `Gt7Ps5MyBestLap` を手動文言を `resolvedText` に指定して再利用し、既存の GT7 用フォーマッターと
プレースホルダー検証を使う。詳細のカテゴリはラップ、プレビューは83456ms。空文言は読まず、自動の
`determineMyBestLap` の状態（personalBestMs 等）は変更しない。開始音・ログのキーは `RemainingFuel.Root` を維持する。

ACE の手動読み上げは、一覧の ▶ と Windows のセットホットキーからの依頼を合流し、
`Simulator.AceWindows.id` 宛てで保存済みセットが見つかり、ACE が選択中の場合だけ処理する。
`manualReadoutItemKeys(Simulator.AceWindows.id)` は既存の `AceWindowsReadoutItemKey.RemainingFuel.Root.value`
（`ace_windows_auto_remaining_fuel`）を返し、手動の「残り燃料」を選択できる。
`currentRemainingFuelPercent(data)` は `AceWindowsFuelData.remainingPercent`（0〜100）を四捨五入した整数％を返す。
未受信・受信から5秒超・非有限・0未満・100超は null とし、シミュレーター切替時は最新値と受信時刻をクリアする。
鮮度判定済みの `AceWindowsManualReadoutItem(key, textTemplate, value)` を保存順に最大3件処理し、
手動専用の `ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT`（「燃料は残り{percent}パーセント」）か
セットの編集文言を `formatAceWindowsRemainingFuelReadoutText` で置換する。
空白文言を除外して「、」で連結し、`AceWindowsManualReadoutText` の1イベントにまとめる。
値が null の項目は「残り燃料はデータがありません」を読む（全項目がデータ無しでも同じ）。
対応項目なしは
「読み上げる項目がありません」、全項目が空白なら空文字とする。
自動読み上げのスイッチ・閾値・`remainingFuelWarned` 等の判定状態は変更せず、
開始音・ログ・常時キュー末尾追加には既存の `RemainingFuel.Root` を使う。
`manualReadoutItemKeys(Simulator.AceWindows.id)` は続けて既存の `AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value`（`ace_windows_auto_remaining_fuel_laps`）を返し、手動の「残り燃料の周回数」を選択できる。
`currentRemainingFuelLaps(data)` は `AceWindowsRemainingFuelLapsData.remainingLaps` の整数部を返し、未受信・受信から5秒超・非有限・0以下・1,000周超は null（データなし）とする。1周未満の正数は従来どおり整数部へ切り捨てて0周を返す。
手動専用の `ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT`（「燃料は残り約{laps}周」）を `formatAceWindowsRemainingFuelLapsReadoutText` で置換し、値が null の項目は「残り燃料の周回数はデータがありません」を読む。
`AceWindowsReadoutItemKey.MyBestLap.Root` も手動項目に含め、最新の `AceWindowsBestLapTimeData.bestLapTimeMs` を、正値の場合に（Android ではサーバーが値の変化時しか送らないため鮮度判定はしない） `formatAceWindowsMyBestLapReadoutText` で整形する。既定文言は `ACE_WINDOWS_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT`（「自己ベストラップ {laptime}」）。

## エネルギーの日本語表記

LMU の Virtual Energy は「バーチャルエネルギー」、省略表記は「エネルギー」に統一する。
読み上げの既定文言とリセット値にもこの表記を使う。保存済みの自由文言はユーザー設定として保持し、自動置換しない。

ACE の手動項目は `RemainingFuel`、`RemainingFuelLaps`、`MyBestLap`、`TyreTemperature` の順。
手動「タイヤ温度」は既存の `AceWindowsReadoutItemKey.TyreTemperature.Root` を使い、
`currentMaxTyreTemperatureCelsius` で `AceWindowsTyreCarcassTemperatureData.wheels` の有限かつ0より大きい
最高値を四捨五入した整数℃を取得する。未受信・受信から5秒超・有効な温度なし・ACE以外への切り替え後は
「タイヤ温度はデータがありません」を読む。既定文言は `ACE_WINDOWS_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT`
（「タイヤ温度{celsius}℃」）。既存の `formatAceWindowsTyreTemperatureReadoutText` と
`findUnknownAceWindowsTyreTemperatureReadoutPlaceholders` を使い、詳細画面ではタイヤカテゴリ、プレビューは90℃。
`AceWindowsTyreTemperatureCurrentValue` の本文をセットの保存順に連結し、空欄は読み上げない。
自動過熱警告のしきい値・ヒステリシス・スイッチ判定は適用せず、警告状態も変更しない。
開始音とログには既存の `RemainingFuel.Root` を使い、常にキュー末尾へ追加する。

音量低下警告の表示設定は `LowVolumeWarningEnabledPreferencesRepository` と
`ObserveLowVolumeWarningEnabledUseCase` / `SaveLowVolumeWarningEnabledUseCase` で扱い、既定値は
`LOW_VOLUME_WARNING_ENABLED_DEFAULT`（オン）とする。


手動読み上げ一覧の再生ボタン表示設定は `MANUAL_READOUT_PLAY_BUTTON_LAYOUT_ENABLED_DEFAULT`（false）を既定値とする。

テレメトリログは読み上げの起点を `NarrationSource` で保持する。手動読み上げは `MANUAL`（`manual`）、それ以外（デバッグ読み上げを含む）は `AUTO`（`auto`）として保存する。


LMUピットタイミングの燃料は `LmuWindowsReadoutItemKey.PitTiming.Fuel`（`lmu_windows_auto_pit_timing_fuel`）で保存する。
詳細のスイッチ・閾値（既定3周、1〜5周）・通常／切迫時文言は既存の PitTiming DataStore を共有し、
ProtoNumber 8〜10を追加する。既存フィールド1〜7は維持し、旧データでは燃料の既定値を使う。
`ObserveLmuWindowsPitTimingEnabledStatesUseCase` の既定値は有効で、Narratorのマージ済み有効状態と
`determinePitTimingFuel` が `PitTiming.Root`／`PitTiming.Fuel` を判定する。
燃料警告は `energySource == LmuWindowsEnergySource.Fuel`（P2/P2Elms/P3/GTE/Unknown）でのみ発火し、
VEクラス・無効設定でも燃料消費の追跡と評価周の更新を続ける。詳細の燃料設定はクラスに関わらず表示し、listPaneは変更しない。
`pitTimingJob` が `exactPercent / 100.0` を一度だけ追跡し、手動の燃料残り周回数と `fuelLapsTrackingState` を共有する。
VE・タイヤ・燃料の候補の最小周回数を選び、共通の `PitTimingLapGate` を通った候補だけ読み上げ履歴へ記録する。
給油周を消費量の推定から除外し、給油で警告履歴を再武装、周回巻き戻りで追跡・評価履歴を初期化する。
通常の既定文言は「燃料は残り約{laps}周」、0周以下は「燃料残り1周未満」。保存文言の解決・開始音・優先度・キュー・ログは
既存の `PitTiming.Root` の流儀を使い、ログに燃料スナップショットと判定状態を含める。
