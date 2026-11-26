# server-connection-domain

サーバー接続（IPの保存・サーバー検出・疎通確認・バージョン取得・接続状態の監視・コンソールIP・ローカルネットワーク権限）のモデル、Repository インターフェース、UseCase。

- `:core:domain` にのみ依存する。パッケージ名は `kurou.kodriver.domain.serverconnection` 配下とする。
- `MdnsConstants` は `:server` も使う。`:server` はこのモジュールに依存できないため `:core:domain` に置く。
- `ConnectionCheckDefaults` は `:core:domain` の `ObserveTelemetryReceivingUseCase` も使うため `:core:domain` に置く。
