# debug-domain

デバッグ表示で使うモデル・Repository インターフェースと UseCase。固定カードからの読み上げ要求と、カードの並び順を扱う。

- `:core:domain` にのみ依存する。パッケージ名は `kurou.kodriver.domain.debug` 配下とする。

## デバッグ読み上げ

`DebugReadoutRequest` と `DebugReadoutRequestRepository`、送信・購読用の `RequestDebugReadoutUseCase` / `ObserveDebugReadoutRequestUseCase` を提供する。`:core:domain` の `ReadoutItemKey.topLevelEntriesOf(simulator)` は一覧項目だけを entries 順に返す。`:core:domain` の `DebugReadoutText` は入力文言を置換せず音声経路へ渡すイベントで、本文の再解決には copy を使う。
