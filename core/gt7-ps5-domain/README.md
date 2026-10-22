# core:gt7-ps5-domain

GT7（Gran Turismo 7 PS5）固有のドメイン層（モデル・Repository インターフェース・UseCase）。

- `:core:domain` に依存する（共有型 `ReadoutItemKey` / `SpeechEvent` 等を参照するため）。
- GT7 固有の sealed key、`GT7_PS5_READOUT_ITEM_KEYS` と登録 spec（`Gt7Ps5ReadoutItemKeys.kt`）、読み上げ既定値は本モジュールに置く。新しい key は必ずキー一覧にも追加する。
- パッケージ名は `kurou.kodriver.domain.gt7ps5` のまま（`:core:domain` と同一パッケージを分割して持つ）。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/core-gt7-ps5-domain.svg)
<!-- MODULE-GRAPH-END -->
