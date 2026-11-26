# designsystem

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/core-designsystem.svg)
<!-- MODULE-GRAPH-END -->

## タイポグラフィ

アプリ全体のタイポグラフィは `Typography.kt` の `KoDriverTypography` で一元管理し、`KoDriverTheme` から `MaterialTheme` へ渡している。現時点では Material3 のデフォルトをそのまま採用している。

- 文字スケールやウェイトを調整する場合は `KoDriverTypography` だけを変更する。
- feature モジュール側では `fontSize` / `FontWeight` を直接指定せず、`MaterialTheme.typography.*` のスタイルだけを参照する。

## 配色

配色は `Color.kt` のトーンパレットと `Theme.kt` の `LightColorScheme` / `DarkColorScheme` で定義している。

- 背景・カードなどの surface 系ロールは、わずかに緑寄りの低彩度グレー（`Neutral*` / `NeutralVariant*`）で統一する。Material 3 ベースラインの紫系ニュートラルが混ざらないよう、`surfaceContainerLowest`〜`surfaceContainerHighest`・`surfaceDim`・`surfaceBright`・`inverseSurface` を含めて明示的に指定する。
- ブランド色の蛍光黄緑（`Yellow*`）は primary にのみ使い、選択状態・スイッチ・強調に絞る。secondary は彩度を落としたオリーブ、tertiary はティールとして役割を分ける。
- `app:shared` の `AppTheme.kt` に同じ配色値を複製しているため、配色を変更する場合は両方を同期させる。

## 角丸（Shapes）

角丸は `Shapes.kt` の `KoDriverShapes` で定義し、`Theme.kt` の `KoDriverTheme` から `MaterialTheme` へ渡している。

`large=16dp`・`extraLarge=28dp` は Material3 の既定値と同じ値を明示しており、見た目は変わらない。

- feature モジュール側では `RoundedCornerShape` を直接指定せず、`MaterialTheme.shapes.*` のスタイルだけを参照する。
- `app:shared` の `AppTheme.kt` に同じ角丸値を複製しているため、角丸を変更する場合は両方を同期させる。

## 余白（Spacing）

余白（padding・Spacer・`Arrangement.spacedBy` 等）は `Spacing.kt` の `KoDriverSpacing`（`extraSmall=4dp` / `small=8dp` / `medium=12dp` / `large=16dp` / `extraLarge=24dp`）で定義している。Material3 の `MaterialTheme` には spacing 用のスロットがないため、`KoDriverShapes` のようにテーマへ渡すのではなく、feature モジュール側が `KoDriverSpacing.*` を直接参照する。

- feature モジュール側では 4/8/12/16/24dp の余白値を直接指定せず、`KoDriverSpacing.*` を参照する。
- 読み上げ文言のタイトルと入力欄は `DetailPaneLabeledTextField` の4dp（`extraSmall`）で揃える。挿入チップと説明文のまとまりには下部8dp（`small`）、同じカード内の独立した文言グループ間には16dp（`large`）を確保する。区切り線やカード内部の既存余白も含めて調整し、一律には加算しない。
- `DetailPaneLabeledTextField` / `DetailPaneCardTextField` は `onPreviewClick = null` で試聴ボタンを非表示にできる。
- アイコンサイズ・カード幅など「余白」ではない寸法（`Modifier.size` 等）は対象外。値がたまたま同じでも `KoDriverSpacing` は使わない。
- `app:shared` は `moduleGraphAssert` で `core:.*` への依存が禁止されているため、`AppSpacing.kt` に同じ余白値を複製している。余白の値を変更する場合は両方を同期させる。
- 4dp グリッドから外れる半端な余白値（18dp・6dp・3dp・10dp 等）の統一は本トークン化のスコープ外（[#1560](https://github.com/ai-kurou/KoDriver/issues/1560) 参照）。

## 一覧ペインの初期幅

list/detail 構成の画面の一覧ペインの初期幅は `KoDriverListPaneWidth.kt` の `KoDriverListPaneWidth`（400.dp）で定義し、`PaneExpansionAnchor.Offset.fromStart` に渡す。

- `app:shared` は `:core:*` に依存できないため、`AppPaneWidth.kt` に同値を別定義している。幅を変更する場合は両方を同期させる。
- デバッグ状態詳細画面の表示列数を切り替える境界値（`calculateDebugStateColumns` 内の 400dp / 700dp）は、この初期幅とは別の定義として扱う。

## 文言入力の状態

- `rememberTextInsertState()` を入力欄ごとに作り、`DetailPaneLabeledTextField` / `DetailPaneCardTextField` の `insertState` に渡す。`insertState.insert("{placeholder}")` でカーソル位置に挿入し、選択範囲があれば置換する。改行除去後に文字数上限を超える要求は挿入せず消費する。
- `PendingTextState.kt` の `rememberPendingText` は非同期保存中の入力を保持し、保存側UseCaseと同じ `trim().take(MAX)` に正規化した保存値が一致すると保存待ちを解除する。文字数上限は呼び出し側から渡す。
- 連続編集・リセットによる保存待ちを順序付きで保持し、途中の保存値が反映されても最新の入力に巻き戻りが起きないようにする。
- 保存反映が conflate されて観測できない場合も、保存待ちの変更から既定で3秒後に入力を保持したまま解除し、その後の外部更新への追従を再開する。

## 読み上げ文言入力

`ReadoutTextField` は `DetailPaneLabeledTextField` を使い、読み上げ文言の入力・試聴・既定値へのリセットを提供する。文字列リソースは呼び出し側で解決し、文字数上限は `maxLength` に渡す。`insertState` でプレースホルダーを挿入できる。

補足文は TTS 不可の `unavailableText`、null でも空でもない `unknownPlaceholdersText`、空欄時の `supportingText` の順に優先する。入力が空白のみでなければ選択アイコンを表示し、警告がなければ補足文を隠す。リセットは `defaultText` を `onTextChanged` に通知する。

`PlaceholderReadoutTextField` は `ReadoutTextField` に保存待ちの入力状態（`rememberPendingText`）とプレースホルダー挿入チップ・説明文を組み合わせた部品。チップの下に説明文を全幅で縦に並べ、長い文言でも説明文が細く折り返されないようにする。保存値は `savedText`、挿入文字列は `placeholder`、チップと説明文は `insertLabel` / `placeholderHint` に渡す。TTS が利用可能で、現在の入力と挿入文字列の合計が `maxLength` 以下の場合だけチップを有効にする。

文字列リソースは呼び出し側で解決する。`unknownPlaceholdersText` は現在の入力を受け取る Composable ラムダで、保存反映前の入力からも `stringResource` を使って警告文を生成できる。入力・挿入・リセットは保存待ちの表示を更新し、`onTextChanged` に通知する。

## 閾値スライダー

- `ThresholdSlider` の `onValueChange` は操作中の表示値を通知する。試聴など兄弟要素で同じ値が必要な画面はPane側で保持し、`value` にも渡す。保存は従来どおり `onValueChangeFinished` で操作完了時に行う。
- `onValueChange` は省略可能で、既存の呼び出し元は内部の表示状態と操作完了時の保存通知をそのまま使用できる。

## スライダーと試聴の設定詳細

`SliderPreviewDetailPaneContent` は、1本のスライダーと試聴ボタンだけで構成される設定詳細画面（読み上げ速度・声の高さ）の共通本体。タイトル・説明・ラベル書式・リセット説明・試聴ボタン文言・試聴用文言は `SliderPreviewDetailTexts` として呼び出し側で解決して渡す。値・範囲・`steps`・デフォルト値も引数で受け取るため、ドメイン上の定数や保存先は知らない。スライダー操作の完了とリセットは `onValueChangeFinished` で、試聴は `onPreviewClicked` で通知する。

## 閾値の保存待ち状態

- `PendingThresholdState.kt` の `rememberPendingThreshold` は操作中の整数閾値と操作完了・リセット後の保存待ちを保持する。`change` は表示だけを更新し、`finishChange` は保存待ちを追加する。
- 途中の保存通知では最新の表示値を巻き戻さず、最後の保存待ちが解除され、操作中でもなければ外部更新に追従する。
- 文言入力と同様、保存通知が省略された場合は保存待ちの変更から既定で3秒後に表示値を保持したまま保存待ちを解除する。操作中の値は期限後も保持する。

## デバッグ読み上げ

`DebugReadoutRequestCard` は項目ドロップダウン、自由文言の OutlinedTextField と読み上げボタンを提供する。domain に依存せずキーの value を文字列で受け取り、既存の `readoutItemDisplayName` で表示する。選択・入力・送信はコールバックで通知し、空白のみではボタンを無効化する。ラベルは composeResources の日本語文字列を使う。

## 一覧の行単位キーボード移動

`ListRowKeyboardNavigation.kt` は、行内のどの部品にフォーカスがあっても ↑↓ で隣の行の主操作領域へ移動する一覧の共通処理を提供する。項目は `String` の項目キーで扱い、domain 型には依存しない。Alt（Mac では Option）+ ↑↓ による行の並べ替えは `handleListRowReorderKey` が（移動元, 移動先）を `onMove` に渡して行い、移動後は同じ行へフォーカスを戻す。

- `ListRowFocusState` が行ごとの `FocusRequester` とフォーカス中の行の項目キーを保持する。並べ替えに追従するため、フォーカス行はインデックスではなく項目キーで記録する。
- `handleColumnRowReorderKey` は LazyList を使わない縦並びの一覧向けで、フォーカス行のインデックスと項目キー一覧から Alt+↑↓ を並べ替え（移動元, 移動先）として処理する（手動タブ詳細の選択項目カードが利用）。
- `handleListRowArrowKey` を `Modifier.onKeyEvent` から呼び出す。先頭の↑・末尾の↓では折り返さず消費だけする。Alt / Ctrl / Shift / Meta 付きの↑↓と、行外にフォーカスがある場合は処理しない。
- 項目の LazyList インデックスが連続しない場合（見出し・区切り行を挟む場合）は、末尾引数 `lazyIndexOf`（項目の並び順のインデックス → LazyList のインデックス）を渡す。省略時は `firstItemLazyIndex` から連続していると見なす。
- 画面外の行は `scrollToItem` でスクロールしてからフォーカスを移す。
- 表示と操作の統合は各 feature のペインのテストで確認し、判定ロジックは `src/jvmTest` の `ListRowKeyboardNavigationTest` で確認する。

### グリッド（複数列）

`ListGridKeyboardNavigation.kt` の `handleListGridArrowKey` は、`LazyVerticalGrid` のような複数列の一覧向けに ↑↓←→ を処理する。項目は左上から右へ、行ごとに並ぶものとして扱い、`ListRowFocusState` を共用する。

- ←→ は同じ行の隣へ、↑↓ は同じ列の上下へ移動する。端では折り返さず、グリッドの外にも出ず、消費だけする。
- 最終行が欠けていて真下の項目がない場合、↓ は最後の項目へ移動する。
- Alt / Ctrl / Shift / Meta 付きの矢印と、項目の外にフォーカスがある場合は処理しない。
- 判定ロジックは `src/jvmTest` の `ListGridKeyboardNavigationTest` で確認する。

## 一覧上部に重ねる部品の出入り

`KoDriverTopFloatingVisibility` は、上からスライドしながらフェードして出入りする（表示300ms・非表示200ms）。自動読み上げ一覧の先頭へ戻るボタンと、テレメトリログ一覧の新着ログボタンが利用する。配置は呼び出し側が `modifier` で指定し、ボタン本体は各 feature に残す。`app:shared` のバナーは `:core:*` に依存できないため、独自に定義している。

## 削除・リセットの確認ダイアログ

`KoDriverDestructiveConfirmDialog` は、error 色の確認ボタンとキャンセルボタンを持つ共通ダイアログ。`title`・`body`・`confirmLabel`・`dismissLabel` は呼び出し側で `stringResource` を解決して渡す。本文への名前の埋め込みも呼び出し側で行い、feature の文字列リソースを designsystem に持ち込まない。

確認ボタンは `ContextClick` ハプティックを1回発生させてから `onConfirm` を呼ぶ。キャンセルと閉じる要求ではハプティックを発生させない。

`onConfirm` は確認ボタン、`onDismiss` はキャンセルボタンとダイアログの閉じる要求を通知する。表示状態と削除・リセット処理は呼び出し側で管理する。

## ヒント行・ヘルプシート行

- `KoDriverHintRow` はラベル・ヘルプアイコンボタン・区切り線と、省略可能な `trailingText`（`labelSmall`）を表示する。ボタンを押すと `sheetContent` を `ModalBottomSheet` に表示する。
- 開閉は部品内の一時的な UI 状態として `rememberSaveable` で保持し、保存状態からの再生成時に復元する。業務状態は従来どおり feature の ViewModel が管理する。
- `KoDriverHintRow` と `HelpIconButton` は、呼び出し元の `LocalHapticFeedback` をシート内へ引き継ぐ。Android ではシートが別の `ComposeView` になって値が上書きされ、引き継がないとハプティックのオフ設定が効かない。ダイアログやシートを新しく作る場合も、`LocalHapticFeedback.current` は外側で読む。
- `KoDriverHintSheetRow` は同じ見た目のアイコンバッジとタイトル・説明を表示する。文字列リソースは呼び出し側で解決して渡す。
- 表示と操作は `src/jvmTest` の `KoDriverHintRowTest`、実際の画面レイアウトは両 list feature の既存スクリーンショットテストで確認する。

## 読み上げ開始音のアイコン

- `ReadoutStartSoundIcons` は読み上げ開始音のアイコンを定義する。設定一覧は `OutlinedOn`（Outlined MusicNote）、自動読み上げ一覧は `FilledOn` / `FilledOff`（Filled MusicNote / MusicOff）を使う。
- 画面ごとに直接 `Icons.*` を指定せず、この値を参照する。

## デバッグ状態の共通部品

- `DebugStateCard` はキーに対応するタイトルリソースと内容のマップを受け取り、無効時は透明度を0.38にする。枠は `frame` で渡し、カードキー固有の装飾は各 feature に残す。
- `DebugStateUnavailableContent` は未取得文言を文字列で受け取り、装飾とともに表示する。リソースの解決は feature が行う。
- `DebugStateHeatTile` は必須の `heatLevel: HeatLevel?` を受け取り、`null` は中立色で表示する。`HeatLevel` とテーマ色への変換 `heatColor` は共通とし、温度・摩耗の閾値判定は各 feature に残す。
- `DebugStateStatusChip` は文言とアクセント色を受け取る。
- `DebugStateReorderableGrid` は長押しで並べ替えられるカードのグリッド（LMU / ACE / GT7 のデバッグ状態詳細が共通利用）。列数は `calculateDebugStateColumns`（幅400dp未満で1列、700dp未満で2列、それ以上で3列）で決める。先頭の固定項目（デバッグ読み上げ依頼カードなど）は `fixedItems` と `fixedItemCount` で渡し、並べ替えの対象にしない。`onMoveCard` には固定項目を除いたインデックスを通知する。テスト用に `DEBUG_STATE_GRID_TEST_TAG` を公開している。カードの中身・タイトル・枠は feature が `card` で組み立てる。
- `DebugStateCardFrame` はタイトル・記号・アクセント色を受け取る。カードキーから色・記号への変換は各 feature の責務とする。`showHandle` で右上の点状装飾を切り替え、GT7 は非表示を維持する。
- `SimulatorInfoContent` は `simulatorId` を受け取り、既存の `simulatorDisplayName` で表示名を解決する。
- 各 feature の既存関数は薄いラッパーとして維持し、温度判定の既定引数と既存テストの呼び出しを保つ。LMU 固有の `DebugStateWearMeter` は共通化しない。

## 開閉アニメーション

`KoDriverCollapsibleVisibility` は、高さを伸縮させながらフェードして出入りする（表示300ms・非表示200ms）。親の `Arrangement.spacedBy` の間隔が残らないよう、非表示中は子要素を配置しない。伸縮の基準は `expandFrom`（既定は上端）で指定する。手動読み上げ詳細のホットキー設定まわりは下端、手動読み上げ一覧のホットキーチップと音声認識カードの履歴は上端を使う。

## 詳細ペインのタイトル

`DetailPaneTopAppBar` のタイトルは1行で表示し、バーの幅に収まらない場合は末尾を省略する。
手動読み上げセット名などの長いタイトルでも、戻るボタン・操作領域と共存できるようにする。

## 一覧の行の選択表示

`ListRowSelectionStyle.kt` は、詳細を開いている行の見た目を自動タブと手動タブで共通化する。`animateListRowSelectionStyle(isSelected)` が背景色・枠線色・影の高さを500msで切り替え、`Modifier.listRowSelection` が影と1dpの枠線を付ける。背景色は呼び出し側のカードに渡す。

その他一覧とテレメトリログ一覧の行は、通常時の背景色や文字色の決め方がカードと異なるため `animateListRowSelectionStyle` は使わず、`listRowSelectionAnimationSpec()` で切り替えの速さだけを揃える。

## 行内の部品の ←→ 移動

`ListRowPartsKeyboardNavigation.kt` の `handleListRowPartArrowKey` は、一覧の1行の中の部品（主操作領域・スイッチ・トグルなど）を ←→ で順に移動する共通処理を提供する。部品は行内の並び順に 0 から番号を振り、`ListRowPartsFocusState` が各部品の `FocusRequester` とフォーカス中の部品を保持する（0 番は行の主操作領域）。

- → で次の部品、← で前の部品（0 番に戻れる）へ移動する。端では折り返さず、行の外にも出ず、消費だけする。
- 無効な部品（`partEnabled` が false）は飛ばす。
- Alt / Ctrl / Shift / Meta 付きの矢印と、行内の部品にフォーカスがない場合は処理しない。↑↓ は従来どおり行単位の移動（`ListRowFocusState`）が担う。

## 行の背景画像

読み上げ項目の行カードの背景画像は `ReadoutBackground` enum で種類を管理する。画像は `composeResources/drawable/<種類>_row_background.jpg` に置く。

- 画像は 600x600 の JPEG で、1枚あたり 150KB 以下に収める（既存の画像はいずれも 150KB 以下）。
- どの項目にどの画像を使うかは各 feature が決める。画像を追加するときは enum に値を追加し、feature 側の対応づけとテストを更新する。
- 複数の画像を1行に並べるときは `rightAlignedBackgroundImages` を使う。右側の領域（幅の65%、1枚あたり高さ基準の上限あり）を枚数で等分し、各画像を中央で切り取って横に並べる。1枚のときは `rightAlignedBackgroundImage` と同じ見た目になる。
- 正方形の画像をタイルなどの領域全面に敷くときは `fullBackgroundImages` を使う。領域を枚数で等分して各画像を中央で切り取り、上に指定色を65%重ねて前面の文字・アイコンを読みやすくする。空のときは何も描かない。

## 一覧の登場アニメーション

`KoDriverListEntrance` は、一覧を開いたときに中身を12dp下からスライドしながら200msでフェードインさせる。自動読み上げ一覧・手動読み上げ一覧・テレメトリログ一覧が利用する。画面いっぱいに広げるかどうかは呼び出し側が `modifier` で指定する。
