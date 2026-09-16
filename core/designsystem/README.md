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
