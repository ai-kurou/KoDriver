# other-readout-start-sound-detail

開始音の試聴は、選択中のシミュレーターの TextToSpeechEngine（`named(Simulator.X.id)`）で再生する。
開始音の設定自体は全シミュレーターで共有する。
ViewModel は simulatorId をキーに保持し、ダイアログを再表示した際も選択中のシミュレーターのエンジンで試聴する。

`OtherReadoutStartSoundDetailFlow` は保存済みの開始音種別と未確定の選択から UI state を合成する冷たい Flow。
`selectedType` は保存値、`pendingType` は未確定の選択があればその値、なければ保存値を使う。
ViewModel は `stateIn` による状態の共有とライフサイクル管理、選択時の試聴、確定時の保存、確定・キャンセル時の未確定選択のリセットを担当する。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-other-readout-start-sound-detail.svg)
<!-- MODULE-GRAPH-END -->
