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
- 読み上げ文言のタイトルと入力欄は `DetailPaneLabeledTextField` の4dp（`extraSmall`）で揃える。挿入チップと説明文の行には下部8dp（`small`）、同じカード内の独立した文言グループ間には16dp（`large`）を確保する。区切り線やカード内部の既存余白も含めて調整し、一律には加算しない。
- `DetailPaneLabeledTextField` / `DetailPaneCardTextField` は `onPreviewClick = null` で試聴ボタンを非表示にできる。
- アイコンサイズ・カード幅など「余白」ではない寸法（`Modifier.size` 等）は対象外。値がたまたま同じでも `KoDriverSpacing` は使わない。
- `app:shared` は `moduleGraphAssert` で `core:.*` への依存が禁止されているため、`AppSpacing.kt` に同じ余白値を複製している。余白の値を変更する場合は両方を同期させる。
- 4dp グリッドから外れる半端な余白値（18dp・6dp・3dp・10dp 等）の統一は本トークン化のスコープ外（[#1560](https://github.com/ai-kurou/KoDriver/issues/1560) 参照）。

## 一覧ペインの初期幅

list/detail 構成の画面の一覧ペインの初期幅は `KoDriverListPaneWidth.kt` の `KoDriverListPaneWidth`（400.dp）で定義し、`PaneExpansionAnchor.Offset.fromStart` に渡す。

- `app:shared` は `:core:*` に依存できないため、`AppPaneWidth.kt` に同値を別定義している。幅を変更する場合は両方を同期させる。
- デバッグ状態詳細画面の `NARROW_WIDTH_UPPER_BOUND` は表示列数を切り替える境界値であり、この初期幅とは別の定義として扱う。

## 文言入力の状態

- `rememberTextInsertState()` を入力欄ごとに作り、`DetailPaneLabeledTextField` / `DetailPaneCardTextField` の `insertState` に渡す。`insertState.insert("{placeholder}")` でカーソル位置に挿入し、選択範囲があれば置換する。改行除去後に文字数上限を超える要求は挿入せず消費する。
- `PendingTextState.kt` の `rememberPendingText` は非同期保存中の入力を保持し、保存側UseCaseと同じ `trim().take(MAX)` に正規化した保存値が一致すると保存待ちを解除する。文字数上限は呼び出し側から渡す。
- 連続編集・リセットによる保存待ちを順序付きで保持し、途中の保存値が反映されても最新の入力に巻き戻りが起きないようにする。
- 保存反映が conflate されて観測できない場合も、保存待ちの変更から既定で3秒後に入力を保持したまま解除し、その後の外部更新への追従を再開する。

## 読み上げ文言入力

`ReadoutTextField` は `DetailPaneLabeledTextField` を使い、読み上げ文言の入力・試聴・既定値へのリセットを提供する。文字列リソースは呼び出し側で解決し、文字数上限は `maxLength` に渡す。`insertState` でプレースホルダーを挿入できる。

補足文は TTS 不可の `unavailableText`、null でも空でもない `unknownPlaceholdersText`、空欄時の `supportingText` の順に優先する。入力が空白のみでなければ選択アイコンを表示し、警告がなければ補足文を隠す。リセットは `defaultText` を `onTextChanged` に通知する。

## 閾値スライダー

- `ThresholdSlider` の `onValueChange` は操作中の表示値を通知する。試聴など兄弟要素で同じ値が必要な画面はPane側で保持し、`value` にも渡す。保存は従来どおり `onValueChangeFinished` で操作完了時に行う。
- `onValueChange` は省略可能で、既存の呼び出し元は内部の表示状態と操作完了時の保存通知をそのまま使用できる。

## 閾値の保存待ち状態

- `PendingThresholdState.kt` の `rememberPendingThreshold` は操作中の整数閾値と操作完了・リセット後の保存待ちを保持する。`change` は表示だけを更新し、`finishChange` は保存待ちを追加する。
- 途中の保存通知では最新の表示値を巻き戻さず、最後の保存待ちが解除され、操作中でもなければ外部更新に追従する。
- 文言入力と同様、保存通知が省略された場合は保存待ちの変更から既定で3秒後に表示値を保持したまま保存待ちを解除する。操作中の値は期限後も保持する。

## デバッグ読み上げ

`DebugReadoutRequestCard` は項目ドロップダウン、自由文言の OutlinedTextField と読み上げボタンを提供する。domain に依存せずキーの value を文字列で受け取り、既存の `readoutItemDisplayName` で表示する。選択・入力・送信はコールバックで通知し、空白のみではボタンを無効化する。ラベルは composeResources の日本語文字列を使う。

## 削除・リセットの確認ダイアログ

`KoDriverDestructiveConfirmDialog` は、error 色の確認ボタンとキャンセルボタンを持つ共通ダイアログ。`title`・`body`・`confirmLabel`・`dismissLabel` は呼び出し側で `stringResource` を解決して渡す。本文への名前の埋め込みも呼び出し側で行い、feature の文字列リソースを designsystem に持ち込まない。

`onConfirm` は確認ボタン、`onDismiss` はキャンセルボタンとダイアログの閉じる要求を通知する。表示状態と削除・リセット処理は呼び出し側で管理する。

## ヒント行・ヘルプシート行

- `KoDriverHintRow` はラベル・ヘルプアイコンボタン・区切り線と、省略可能な `trailingText`（`labelSmall`）を表示する。ボタンを押すと `sheetContent` を `ModalBottomSheet` に表示する。
- 開閉は部品内の一時的な UI 状態として `rememberSaveable` で保持し、保存状態からの再生成時に復元する。業務状態は従来どおり feature の ViewModel が管理する。
- `KoDriverHintSheetRow` は同じ見た目のアイコンバッジとタイトル・説明を表示する。文字列リソースは呼び出し側で解決して渡す。
- 表示と操作は `src/jvmTest` の `KoDriverHintRowTest`、実際の画面レイアウトは両 list feature の既存スクリーンショットテストで確認する。

## デバッグ状態の共通部品

- `DebugStateCard` はキーに対応するタイトルリソースと内容のマップを受け取り、無効時は透明度を0.38にする。枠は `frame` で渡し、カードキー固有の装飾は各 feature に残す。
- `DebugStateUnavailableContent` は未取得文言を文字列で受け取り、装飾とともに表示する。リソースの解決は feature が行う。
- `DebugStateHeatTile` は必須の `heatLevel: HeatLevel?` を受け取り、`null` は中立色で表示する。`HeatLevel` とテーマ色への変換 `heatColor` は共通とし、温度・摩耗の閾値判定は各 feature に残す。
- `DebugStateStatusChip` は文言とアクセント色を受け取る。
- `DebugStateCardFrame` はタイトル・記号・アクセント色を受け取る。カードキーから色・記号への変換は各 feature の責務とする。`showHandle` で右上の点状装飾を切り替え、GT7 は非表示を維持する。
- `SimulatorInfoContent` は `simulatorId` を受け取り、既存の `simulatorDisplayName` で表示名を解決する。
- 各 feature の既存関数は薄いラッパーとして維持し、温度判定の既定引数と既存テストの呼び出しを保つ。LMU 固有の `DebugStateWearMeter` は共通化しない。
