# manual-readout-list

手動読み上げセットの一覧・依頼・ナビゲーションを担う feature モジュール。シミュレーター ID と表示名を受け取り、選択中シミュレーターの保存済みセット一覧を ViewModel で観測する。保存済みセットが0件なら説明シート付きの見出しと、選択中シミュレーター名入りの空状態を表示する。1件以上なら説明見出し行の下に、保存済みセットを1件1行のカードで表示する。各行には一覧上の番号による「セット N」と項目数（空なら「項目なし」）を表示する。右下の「セットを追加」拡張 FAB は常時表示し、UUID を ID とする空のセットを`AddManualReadoutSetUseCase` を通じて保存済みセットの末尾へアトミックに追加する。LMU・GT7・ACE の行の右端の ▶ は `RequestManualReadoutUseCase` で `ManualReadoutRequest(simulatorId, setId)` を送る。`canReadSets` は全シミュレーターで true。ACE の対象セットに残り燃料・残り燃料の周回数・自己ベストラップ・タイヤ温度、GT7 の対象セットに残り燃料・残り燃料の周回数・タイヤ温度・自己ベストラップ、または LMU の対象セットに仮想エネルギー残量・タイヤ摩耗・ブレーキ残量・ブレーキ温度・タイヤ温度・自己ベストラップ・エネルギーの残り周回数・タイヤ摩耗の残り周回数の項目が含まれる場合は、保存した項目順に現在値の文を「、」でつないだ1イベントとして自動読み上げスイッチに関係なく読み、キュー設定に関係なく常にキューの末尾に並べる。対象項目がないセットは、データの有無に関係なく編集不可の固定文言「読み上げる項目がありません」を開始音付きで読み上げ、常にキュー末尾へ追加してログを保存する。セットが見つからない場合は読み上げない。一部の項目が未受信・鮮度切れなら「<項目名>はデータがありません」を連結し、LMU の全項目がデータ無しなら「データがありません」だけを開始音付きで読み上げ、常にキュー末尾へ追加してログを保存する。行タップで詳細を開く。行を長押しすると「削除」メニューを表示し、確認ダイアログで確定すると `DeleteManualReadoutSetUseCase` でセットを削除する。選択中のセットを削除すると選択が解除され詳細も閉じる。詳細は `ManualReadoutContent` のスロットとして受け取り、詳細モジュールへの依存は持たない。詳細スロットに `canNavigateBack` と `onBack` を渡し、戻るボタンと削除後の通知で一覧に戻る。説明シートでは読み上げ・順番待ち・データがないときの動作・長押しによる削除を説明し、Windows デスクトップ版のみ（macOS/Linux では表示しない）ホットキーの説明も表示する。アプリの「手動」タブから表示され、離脱時に選択は解除せず、他タブから戻ると選択中のセットの詳細を引き続き表示する。選択はシミュレーターごとの ViewModel が保持する。

選択中の「手動」タブを再タップすると、セット選択中は選択を解除して一覧へ戻り、一覧表示中は先頭へアニメーションスクロールする。

Windows デスクトップでは、各セット行の左側にセット名・項目数に続けてホットキーチップを表示する。割り当て済みは `Ctrl+Shift+A` のように修飾キーを Ctrl → Alt → Shift の順で空白なしに連結し、未割り当ては枠線のみの「未設定」を表示する。非対応環境ではチップを表示しない。

## 主要なファイルの役割

- `ManualReadoutContent.kt` / `ManualReadoutNavigationState.kt`: Adaptive scaffold と Navigation 3 の list/detail 切替・戻る操作。選択は一覧 ViewModel の UiState から同期する。

- `ManualReadoutListUiState.kt` / `ManualReadoutListViewModel.kt`: 保存済みセット一覧を `StateFlow` で提供し、空のセットを追加・保存し、シミュレーター ID とセット ID を指定した読み上げ依頼を送る。
- `ManualReadoutListModule.kt`: `named(simulatorId)` の Repository を使う ViewModel の Koin 登録。
- `ManualReadoutListViewModelTest.kt` / `ManualReadoutListModuleTest.kt`: 空一覧・一覧更新・末尾への追加保存・全シミュレーターの観測・保存・読み上げ依頼の Repository 解決を確認する JVM テスト。
- `ManualReadoutListPaneWiringTest.kt`: Pane 経由の状態表示・セット追加・読み上げ依頼の配線とシミュレーター切り替えを確認する JVM テスト。
- `ManualReadoutListPane.kt`: 手動読み上げセット一覧の見出し・保存済みセットの行・説明シート・空状態・セット追加 FAB を表示する UI。
- `IsManualReadoutHotkeySupported.kt` / `IsManualReadoutHotkeySupported.android.kt` / `IsManualReadoutHotkeySupported.jvm.kt`: ホットキー説明と行のチップの表示に使う対応判定。Windows デスクトップのみ true を返す。
- `src/commonMain/composeResources/values/strings.xml`: 見出し・セットのタイトルと項目数・読み上げボタン・説明シート・空状態・セット追加 FAB の表示文言。
- `ManualReadoutHotkeyLabel.kt` / `ManualReadoutHotkeyLabelTest.kt`: 一覧専用のラベル生成と、全修飾キー・英数字・未割り当て・範囲外コードの検証。
- `ManualReadoutListPaneTest.kt`: ホットキーチップの割り当て済み・未設定・非対応時の表示分岐と、Content のセット追加 FAB の表示とクリック・見出し・空状態の表示分岐・複数セットのタイトルと項目数・読み上げボタンの有効・無効状態とセット ID の送信・ヘルプの表示とホットキー説明の表示分岐を確認する JVM テスト。
- `ManualReadoutListPaneScreenshotTest.kt`: 見出し・セット追加 FAB 付きの空状態・読み上げボタンが有効な LMU の保存済みセット一覧と説明シート（ホットキーあり・なし）を確認する Desktop/JVM 向けスクリーンショットテスト。
- `ManualReadoutHintSheetContentScreenshotTest.kt`: Android 版の説明シート（ホットキーの説明なし）を確認する Android 向けスクリーンショットテスト。Windows デスクトップ版と表示内容が異なるため追加している。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-manual-readout-list.svg)
<!-- MODULE-GRAPH-END -->

手動ブレーキ残量は `BrakeWear.Root` に対応し、4輪の `remainingPercent`（0〜100）の最小値を四捨五入して、既定文言「ブレーキ残量{percent}%」へ置換する。セットの保存順に他項目と連結し、先頭項目の開始音と常時キュー追加を使う。未受信・受信から5秒超・空の wheels はデータ無しとして扱い、自動警告の前回値・警告済み状態は変更しない。

## 仕様（合意済み）

- 位置: 「自動読み上げ」と「ログ」の間。LMU・ACE・GT7 の全シミュレーター、Windows版と Android版の両方に出す。
- 一覧の1行が1セット（1つのキーに複数の項目）。行の▶をタップすると読み上げ、行のそれ以外は詳細（編集）を開く。セットの初期状態は空。
- 計算と読み上げは各 Narrator が担当し、タブとホットキーは「このセットを読んで」という依頼を出すだけ（既存のF9 `readoutHotkeyJob` と同じ形）。蓄積値を使う読み上げ（ピットタイミング等）にも対応する。
- 自動読み上げの ON/OFF とは無関係に読む。自動警告の「読み上げ済み」状態は変えない。
- 文言は編集できる。4輪ある項目は1輪。開始音は鳴らす。データが無い時は「データがありません」と読む。
- 読み上げ中や自動読み上げとぶつかった時は、キューの末尾に並べる。テレメトリログにもオーバーレイにも出す。
- ホットキー（Windowsのみ）: 修飾キーは Ctrl+Alt+Shift（初期値）・Ctrl+Alt・Ctrl+Shift・Alt+Shift の4種類からセットごとに選ぶ ＋ 数字か英字（Fキーは使わない）。キーを割り当てないセットも作れる。競合判定は「修飾キー＋キー」の組み合わせ単位で、同じ組み合わせは選べないようにする。デバッグ用のVEホットキー（Ctrl+Alt+Shift）とは、修飾キーが Ctrl+Alt+Shift のときだけ重複チェックの対象にする。登録状態（競合/再試行）をセットの詳細画面に表示する。
- デバッグ状態詳細のホットキー設定は今のまま残す。
- 保存はシミュレーターごと。Windows版と Android版は端末ごとのローカル保存なので、OS別のキー分けなどの実装は不要。

- 保存先は `manual_readout_set_preferences_${simulator.id}.pb`。`lmu_windows_readout_hotkey_preferences.pb` はデバッグ用ホットキーの保存先として残す。
- おすすめセット・Android の運転中モード・セット数の上限・ログ上での手動/自動の区別は別フェーズ（未実装）。
- 現状は LMU の仮想エネルギー残量・タイヤ摩耗・ブレーキ残量・ブレーキ温度・タイヤ温度・自己ベストラップ・エネルギーの残り周回数・タイヤ摩耗の残り周回数に対応済み。GT7 は「残り燃料」「残り燃料の周回数」「タイヤ温度」「自己ベストラップ」に対応済み。セット内の保存文言（既定は「燃料は残り{percent}パーセント」）に現在の整数％を埋め、未受信・受信から5秒超は「残り燃料はデータがありません」を読む。対応項目なしの固定文言と常時キュー追加・ログ保存は維持する。ACE は「残り燃料」「残り燃料の周回数」「自己ベストラップ」「タイヤ温度」に対応済み（自己ベストのみ鮮度判定なし）。周回数は `AceWindowsRemainingFuelLapsData.remainingLaps` の整数部を使い、未受信・鮮度切れ・不正値は「残り燃料の周回数はデータがありません」を読む。既存の `RemainingFuel.Root` キーを手動項目に使い、受信から5秒以内の `remainingPercent` を四捨五入した整数％で読む。未受信・鮮度切れ・不正値なら「残り燃料はデータがありません」を読む。

手動ブレーキ温度は `BrakeTemperature.Root` に対応し、`LmuWindowsBrakeTemperatureData.wheels` の最高温度を四捨五入した整数℃を、既定文言「ブレーキ温度{celsius}℃」へ置換する。未受信・受信から5秒超・空の wheels はデータ無しとして扱う。セットの保存順に他項目と連結し、先頭項目の開始音と常時キュー追加を使う。自動過熱警告の前回値・警告済み状態は変更しない。

手動タイヤ温度は `TyreTemperature.Root` に対応し、`LmuWindowsTyreCarcassTemperatureData.wheels` の `CelsiusReading.value` の最大カーカス温度を四捨五入した整数℃を、既定文言「タイヤ温度{celsius}℃」へ置換する。未受信・受信から5秒超・空の wheels はデータ無しとして扱う。セットの保存順に他項目と連結し、先頭項目の開始音と常時キュー追加を使う。自動過熱・低温警告の前回値・警告済み状態は変更しない。

手動自己ベストラップは `LmuWindowsReadoutItemKey.MyBestLap.Root.value` に対応し、セッションの `timing.bestLapTimeMs` を読む。既定文言は `LMU_WINDOWS_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT`（「自己ベストラップ {laptime}」）。既存の `formatLmuWindowsMyBestLapReadoutText` で `{laptime}` を「1分23秒456」形式に置換する。未受信・最後の受信から `CURRENT_VALUE_MAX_AGE_MS`（5秒）超・0以下はデータ無しとし、複数項目では「自己ベストラップはデータがありません」を連結、全項目がデータ無しなら「データがありません」のみ。自動の `MyBestLap.Root` / `DetailEnabled` スイッチとは独立し、`determineMyBestLap`・`personalBestMs` の自動更新判定の状態は変更しない。保存順に連結した1イベントを常にキュー末尾へ追加し、開始音・優先度・ログには先頭項目のキーを使う。

手動エネルギー残り周回数は `LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value` に対応し、蓄積済みの `pitTimingVirtualEnergyTrackingState` から `(currentValue / lastValidLapConsumption).toInt()` で算出する。既定文言は `LMU_WINDOWS_MANUAL_READOUT_VIRTUAL_ENERGY_LAPS_TEXT_DEFAULT`（「エネルギーは残り約{laps}周」）で、1周以上なら `formatLmuWindowsPitTimingReadoutText` で置換し、0周なら固定文言「エネルギー残り1周未満」を読む。蓄積前（`currentLap == -1`）、消費量が null・0以下、算出結果が負、最新エネルギーが未受信・受信から5秒超の場合はデータ無しとし、複数項目では「エネルギーの残り周回数はデータがありません」を連結、全項目がデータ無しなら「データがありません」のみ。自動のスイッチ・閾値・ベストラップ経過時間には依存せず、追跡状態・自動判定状態（`lastAnnouncedPitTimingVirtualEnergyLaps` 等）は変更しない。保存順の1イベントを常にキュー末尾へ追加し、先頭がこの項目なら開始音・優先度・ログには `PitTiming.Root` を使う。タイヤ摩耗の残り周回数は別項目として扱う。

手動タイヤ摩耗残り周回数は `LmuWindowsReadoutItemKey.PitTiming.TyreWear.value`（項目名「タイヤ摩耗の残り周回数」）に対応し、蓄積済みの `pitTimingTyreWearTrackingState` からエネルギーと同じ式で算出する（共通の private ヘルパーを `manualPitTimingTyreWearRemainingLaps` から呼ぶ）。既定文言は `LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_LAPS_TEXT_DEFAULT`（「タイヤは残り約{laps}周」）で、1周以上なら `formatLmuWindowsPitTimingReadoutText` で置換し、0周なら固定文言「タイヤ残り1周未満」を読む（空欄テンプレートなら0周でも空文字）。蓄積前・消費量が null・0以下・非有限、算出結果が負、最新タイヤ摩耗が未受信・受信から5秒超の場合はデータ無しとし、`PitTiming.Root` を開始音・優先度・ログに使う。エネルギーの残り周回数とは別項目で、既存の「タイヤ摩耗」(%) 項目とも別。


GT7 の残り燃料の周回数は保存文言（既定「燃料は残り約{laps}周」）に、燃料追跡の平均消費から推定した整数周回数を埋める。
自動読み上げが無効でも追跡し、しきい値・重複抑止・周回内タイミングに関係なく読む。
0周以下は「燃料残り1周未満」、推定不能・未受信・受信から5秒超は「残り燃料の周回数はデータがありません」。
空欄の項目は値の有無にかかわらず連結しない。

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

セット名は詳細画面でインライン編集できる（最大20文字、重複可）。未設定時は「セット N」を表示する。
名前は表示専用で、読み上げ文には含めない。ACE・LMU・GT7 共通仕様。

項目選択肢は詳細側の `manualReadoutItemKeys(simulatorId)` から取得するため、一覧側にシミュレーター別の候補は持たない。
ACE の空セット追加・詳細遷移・一覧の ▶ の配線は既存経路を使う。

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
