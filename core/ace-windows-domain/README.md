# core:ace-windows-domain

ACE（Assetto Corsa EVO）固有のドメイン層（モデル・Repository インターフェース・UseCase）。

- `:core:domain` に依存する（共有型 `ReadoutItemKey` / `SpeechEvent` 等を参照するため）。
- `:core:domain` は本モジュールに依存しない。`:core:domain` から参照されるACE定数・デフォルト値・`ObserveAceWindowsStatusUseCase` などは `:core:domain` の `domain.acewindows` パッケージに残している。ユーザー設定のデフォルト値（`*Defaults.kt`）は規約により `:core:domain` に置く。
- パッケージ名は `kurou.kodriver.domain.acewindows` のまま（`:core:domain` と同一パッケージを分割して持つ）。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/core-ace-windows-domain.svg)
<!-- MODULE-GRAPH-END -->
