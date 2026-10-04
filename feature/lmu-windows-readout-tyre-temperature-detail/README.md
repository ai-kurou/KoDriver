# lmu-windows-readout-tyre-temperature-detail

過熱・低温警告の自由文言は `{celsius}` に対応する。実際の読み上げでは、警告判定時の全輪の最高カーカス温度を四捨五入した整数（℃）に置換する。低温警告も最高温度を使い、`{wheel}` は未対応。

挿入チップは入力末尾へ `{celsius}` を追加し、文字数上限を超える場合とTTS利用不可時は無効になる。未知のプレースホルダーは警告を表示し、そのまま読み上げる。試聴は実測値ではなく試聴専用の代表値（過熱100℃・低温60℃）を使い、空白・TTS利用不可・音量ゼロ以下では再生しない。既定文言は変更せず、リセットも従来の文言に戻す。

Narrator は判定時に解決した本文を `resolvedText` に保持し、キュー待機中の設定変更で発話とログがずれることを防ぐ。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-lmu-windows-readout-tyre-temperature-detail.svg)
<!-- MODULE-GRAPH-END -->
