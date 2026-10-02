# lmu-windows-readout-vehicle-approach-detail

接近開始時・接近継続時とも左右それぞれの自由文言を入力・保存し、個別に試聴できる（上限30文字、空白は読み上げない）。TTSを利用できない端末では案内を表示し入力・試聴を無効にする。試聴は音量が正の場合にRootキーの開始音を鳴らしてからOS標準TTSで読み上げる。継続時の試聴も開始時と同じ音量・Rootキーの開始音を使う。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-lmu-windows-readout-vehicle-approach-detail.svg)
<!-- MODULE-GRAPH-END -->

継続時の読み上げ文言も開始時と同じ入力欄（上限30文字、空白は読み上げない）で左右別に編集・試聴できる。TTS不可の案内文のみ継続用の文言を使う。
