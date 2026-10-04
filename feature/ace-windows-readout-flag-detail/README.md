# ace-windows-readout-flag-detail

Checkered・White・Green・Red・Blue・Yellow・Black・BlackWhite の8種のカードは自由文言入力・OS標準TTS試聴・デフォルトに戻す操作を提供する。
文言は `ace_windows_flag_readout_text_preferences.pb` に保存し、有効状態の既存保存先は維持する。
試聴は `Flag.Root` の開始音を使い、空白文言・TTS利用不可・音量0以下では再生しない。
OrangeCircle・RedYellowStripes の2種のカードは従来のWAV試聴チップを維持する。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-ace-windows-readout-flag-detail.svg)
<!-- MODULE-GRAPH-END -->
