# manual-readout-list

手動読み上げタブの list/detail 構成を提供する feature モジュール。シミュレーター ID と表示名を受け取り、選択中シミュレーターの保存済みセット一覧を ViewModel で観測する。保存済みセットが0件なら説明シート付きの見出しと、選択中シミュレーター名入りの空状態を表示する。1件以上なら説明見出し行の下に、保存済みセットを1件1行のカードで表示する。各行には一覧上の番号による「セット N」と項目数（空なら「項目なし」）を表示する。右下の「セットを追加」拡張 FAB は常時表示し、UUID を ID とする空のセットを`AddManualReadoutSetUseCase` を通じて保存済みセットの末尾へアトミックに追加する。LMU の行の右端の ▶ は `RequestManualReadoutUseCase` で `ManualReadoutRequest(simulatorId, setId)` を送る。`canReadSets` が false の ACE・GT7 では無効にする。対象セットに仮想エネルギー残量・タイヤ摩耗・ブレーキ残量・ブレーキ温度の項目が含まれる場合は、保存した項目順に現在値の文を「、」でつないだ1イベントとして自動読み上げスイッチに関係なく読み、キュー設定に関係なく常にキューの末尾に並べる。対象項目がないセットは、データの有無に関係なく編集不可の固定文言「読み上げる項目がありません」を開始音付きで読み上げ、常にキュー末尾へ追加してログを保存する。セットが見つからない場合は読み上げない。一部の項目が未受信・鮮度切れなら「<項目名>はデータがありません」を連結し、全項目がデータ無しなら「データがありません」だけを開始音付きで読み上げ、常にキュー末尾へ追加してログを保存する。行タップで詳細を開く。行を長押しすると「削除」メニューを表示し、確認ダイアログで確定すると `DeleteManualReadoutSetUseCase` でセットを削除する。選択中のセットを削除すると選択が解除され詳細も閉じる。詳細は `ManualReadoutContent` のスロットとして受け取り、詳細モジュールへの依存は持たない。詳細スロットに `canNavigateBack` と `onBack` を渡し、戻るボタンと削除後の通知で一覧に戻る。説明シートでは読み上げ・順番待ち・データがないときの動作・長押しによる削除を説明し、Windows デスクトップ版のみ（macOS/Linux では表示しない）ホットキーの説明も表示する。アプリの「手動」タブから表示され、離脱時に ViewModel の選択を解除し、シミュレーター切り替え後やタブの再表示時は一覧に戻る。

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
- おすすめセット・Android の運転中モード・セット数の上限・ACE/GT7 の読み上げ項目・ログ上での手動/自動の区別は別フェーズ（未実装）。
- 現状は LMU の仮想エネルギー残量・タイヤ摩耗・ブレーキ残量・ブレーキ温度に対応済み。タイヤ温度などは1項目ずつ別PRで追加する方針。

手動ブレーキ温度は `BrakeTemperature.Root` に対応し、`LmuWindowsBrakeTemperatureData.wheels` の最高温度を四捨五入した整数℃を、既定文言「ブレーキ温度{celsius}℃」へ置換する。未受信・受信から5秒超・空の wheels はデータ無しとして扱う。セットの保存順に他項目と連結し、先頭項目の開始音と常時キュー追加を使う。自動過熱警告の前回値・警告済み状態は変更しない。
