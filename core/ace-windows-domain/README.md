# core:ace-windows-domain

ACE（Assetto Corsa EVO）固有のドメイン層（モデル・Repository インターフェース・UseCase）。

- `:core:domain` に依存する（共有型 `ReadoutItemKey` / `SpeechEvent` 等を参照するため）。
- ACE 固有の sealed key、`ACE_WINDOWS_READOUT_ITEM_KEYS` と登録 spec（`AceWindowsReadoutItemKeys.kt`）、読み上げ既定値は本モジュールに置く。新しい key は必ずキー一覧にも追加する。
- パッケージ名は `kurou.kodriver.domain.acewindows` のまま（`:core:domain` と同一パッケージを分割して持つ）。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/core-ace-windows-domain.svg)
<!-- MODULE-GRAPH-END -->
