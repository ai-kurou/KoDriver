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

- feature モジュール側では `RoundedCornerShape` を直接指定せず、`MaterialTheme.shapes.*` のスタイルだけを参照する。
- `app:shared` の `AppTheme.kt` に同じ角丸値を複製しているため、角丸を変更する場合は両方を同期させる。

## 余白（Spacing）

余白（padding・Spacer・`Arrangement.spacedBy` 等）は `Spacing.kt` の `KoDriverSpacing`（`extraSmall=4dp` / `small=8dp` / `medium=12dp` / `large=16dp` / `extraLarge=24dp`）で定義している。Material3 の `MaterialTheme` には spacing 用のスロットがないため、`KoDriverShapes` のようにテーマへ渡すのではなく、feature モジュール側が `KoDriverSpacing.*` を直接参照する。

- feature モジュール側では 4/8/12/16/24dp の余白値を直接指定せず、`KoDriverSpacing.*` を参照する。
- アイコンサイズ・カード幅など「余白」ではない寸法（`Modifier.size` 等）は対象外。値がたまたま同じでも `KoDriverSpacing` は使わない。
- `app:shared` は `moduleGraphAssert` で `core:.*` への依存が禁止されているため、`AppSpacing.kt` に同じ余白値を複製している。余白の値を変更する場合は両方を同期させる。
- 4dp グリッドから外れる半端な余白値（18dp・6dp・3dp・10dp 等）の統一は本トークン化のスコープ外（[#1560](https://github.com/ai-kurou/KoDriver/issues/1560) 参照）。
