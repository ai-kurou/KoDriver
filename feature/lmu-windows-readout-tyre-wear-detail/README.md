# lmu-windows-readout-tyre-wear-detail

LMU のタイヤ摩耗を読み上げる機能の詳細設定画面。摩耗警告の有効/無効、閾値（10〜90%）の設定・デフォルト値へのリセット、警告音声の試聴を提供する。有効状態と閾値は DataStore に永続化され、Narrator の読み上げ判定に反映される。

「残存率閾値」はタイヤの残存率（%）に対する閾値を指し、摩耗率ではありません。燃料・バーチャルエナジーの「残量閾値」とは計測対象に合わせて表記を区別しています。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-lmu-windows-readout-tyre-wear-detail.svg)
<!-- MODULE-GRAPH-END -->
