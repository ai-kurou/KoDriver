# NarratorViewModel は共通化しない

`Gt7Ps5NarratorViewModel`（`feature:gt7-ps5-narrator`）・`LmuWindowsNarratorViewModel`（`feature:lmu-windows-narrator`）・`AceWindowsNarratorViewModel`（`feature:ace-windows-narrator`）は、いずれも「テレメトリFlowを購読し、`Determine*NarratorReadoutUseCase` で読み上げ判定した結果を `NarratorEventProcessor` 系のクラスへ渡す」という共通の骨格を持つが、シミュレーターごとに `ReadoutItemKey` の種類・判定対象のテレメトリ項目・購読するUseCase群が異なる。この骨格の類似を理由に、共通ViewModel基底やシミュレーター横断の共通購読ロジックへ切り出すことはしない。

- 各featureモジュールの独立性（`CLAUDE.md` の「モジュール構成」を参照）を優先する。共通基底に切り出すと、シミュレーターごとに異なるUseCase群・判定対象・`ReadoutItemKey`を型パラメータやコールバック注入で表現することになり、素直な `flatMapLatest`/`onEach` の配線より可読性が落ちやすい。
- シミュレーターが増える・読み上げ項目が増えるたびに各NarratorViewModelの構造が微妙にズレていくため（[`readout-item-key-wiring.md`](readout-item-key-wiring.md) も参照）、無理に共通基底へ合わせ込むと将来の項目追加のたびに歪みが生じやすい。
- 各NarratorViewModelを独立させておくことで、モックが単純なテストのまま保てる。共通基底化すると基底クラス側のテストとサブクラス固有のテストの両方が必要になり、テストの複雑さが増す。

新しいシミュレーター向けのNarratorViewModelを実装する際は、既存の類似ViewModel（`CLAUDE.md` の「実装前の類似コード確認」を参照）の構成をそのままコピーして書いてよい。
