# gt7-ps5-narrator

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-gt7-ps5-narrator.svg)
<!-- MODULE-GRAPH-END -->

燃料残り周回数は保存した自由文言をOS標準TTSで読み上げる。`{laps}` は推定残り周回数に置換し、
0周以下では燃料なし用の文言を使う。空白文言・TTS利用不可の場合は読み上げず、WAVにはフォールバックしない。
判定時に文言を確定してイベントに保持するため、キュー待機中に設定が変わってもログと発話は一致する。
詳細ペインの試聴も同じナレーターエンジン経由でTTSを使用する。
