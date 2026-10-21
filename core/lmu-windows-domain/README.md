# core:lmu-windows-domain

LMU（Le Mans Ultimate）固有のドメイン層（モデル・Repository インターフェース・UseCase）。

- `:core:domain` に依存する（共有型 `ReadoutItemKey` / `SpeechEvent` 等を参照するため）。
- `:core:domain` は本モジュールに依存しない。`:core:domain` から参照されるLMU定数・`ObserveLmuWindowsUseCase` などは `:core:domain` の `domain.lmuwindows` パッケージに残している。
- パッケージ名は `kurou.kodriver.domain.lmuwindows` のまま（`:core:domain` と同一パッケージを分割して持つ）。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/core-lmu-windows-domain.svg)
<!-- MODULE-GRAPH-END -->
