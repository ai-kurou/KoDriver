# lmu-windows-readout-vehicle-approach-detail

接近開始時・接近継続時とも左右それぞれの自由文言を入力・保存し、個別に試聴できる（上限30文字、空白は読み上げない）。各文言は編集すると「デフォルトに戻す」ボタンで既定値へ戻せる。既定値のままではリセットできない。TTSを利用できない端末では案内を表示し入力・試聴・リセットを無効にする。試聴は音量が正の場合にRootキーの開始音を鳴らしてからOS標準TTSで読み上げる。継続時の試聴も開始時と同じ音量・Rootキーの開始音を使う。

文言欄は最新入力の正規化値と保存値が異なる間、古い保存結果による巻き戻りを防ぐ。前後の空白除去・30文字制限後の保存値が一致したら待機を解除し、以降の外部更新を反映する。保存値が変わらない入力でも待機を解除する。編集直後に編集前の保存値へ戻す操作の競合は [#1884](https://github.com/ai-kurou/KoDriver/issues/1884) で別途扱う。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-lmu-windows-readout-vehicle-approach-detail.svg)
<!-- MODULE-GRAPH-END -->

継続時の読み上げ文言も開始時と同じ入力欄（上限30文字、空白は読み上げない）で左右別に編集・試聴できる。TTS不可の案内文のみ継続用の文言を使う。

車両接近の動作設定は `LmuWindowsVehicleApproachPreferencesRepository`、開始・継続の左右の読み上げ文言は `LmuWindowsVehicleApproachReadoutTextPreferencesRepository` を使用する。両者は既存の同じ DataStore を共有する。
