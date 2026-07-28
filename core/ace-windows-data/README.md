# ace-windows-data

Assetto Corsa EVOのWindows共有メモリ（Graphicsブロック）を読み取り、ドメイン層のRepositoryを実装するJVM専用モジュールです。
共有メモリI/Oの汎用部分は `core:windows-shared-memory` に切り出しており、本モジュールはAce固有の構造体パースに専念します。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/core-ace-windows-data.svg)
<!-- MODULE-GRAPH-END -->
