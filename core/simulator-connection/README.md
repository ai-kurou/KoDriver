# core:simulator-connection

LMU・ACE・GT7 の接続状態表示（`:feature:lmu-windows-connection` / `:feature:ace-windows-connection` / `:feature:gt7-ps5-connection`）で共通化できる部品を置くためのモジュール。

- 現時点では中身は空（モジュールの土台のみ）。
- 他のモジュールには依存しない。`Simulator` 型など `:core:domain` の型は参照できないため、シミュレーターの種類に依存しない部品だけを置く。
- KoDriver サーバーへの接続（`:feature:server-connection`）は対象外。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/core-simulator-connection.svg)
<!-- MODULE-GRAPH-END -->
