# lmu-windows-domain

LMU Windows 固有のドメインモデル・リポジトリ抽象・純粋な読み上げ判定を提供する。
共通の基盤は `:core:domain` に依存する。パッケージは `kurou.kodriver.domain.*` を維持する。
`LmuWindowsReadoutCatalog` は LMU の読み上げキー・既定値・既定順序を公開し、
全シミュレーターの集約は `:core:data` の `APP_READOUT_CATALOG` が組み立てる。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/core-lmu-windows-domain.svg)
<!-- MODULE-GRAPH-END -->

## LMU設定

`LmuWindowsPitTimingPreferencesRepository` と読み上げ文言Repositoryは、ピットタイミング設定を扱う。
`LmuWindowsTyreTemperaturePreferencesRepository` と読み上げ文言Repositoryは、温度閾値・有効状態・低温警告対象フェーズを扱う。
実装とFactoryは `:core:data` にあり、保存ファイル・フィールド・ProtoNumberは維持するため設定移行は不要。
