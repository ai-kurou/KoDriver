# auto-readout-domain

自動読み上げの有効状態・読み上げ順序・設定リセットの Repository インターフェースと UseCase。

- `:core:domain` にのみ依存する。パッケージ名は `kurou.kodriver.domain.autoreadout` 配下とする。
- 有効状態・順序の既定値（`READOUT_ENABLED_STATE_DEFAULT`・`readoutEnabled`・`defaultAutoReadoutOrder`）は `ReadoutItemKeyRegistry` から取り出すもので、`readoutEnabled` はシミュレーター固有ドメインも使うため `:core:domain` に置く（ドメインモジュール同士は依存できないため）。

`ResetAutoReadoutSettingsUseCase` は受け取った `AutoReadoutSettingsResetter` を順番に実行し、全シミュレーターの自動読み上げ設定を一括で既定値に戻します。途中で失敗した場合は例外を呼び出し側に伝播します。
