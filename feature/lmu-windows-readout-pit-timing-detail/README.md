# lmu-windows-readout-pit-timing-detail

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-lmu-windows-readout-pit-timing-detail.svg)
<!-- MODULE-GRAPH-END -->

バーチャルエナジー・タイヤ摩耗それぞれの通常文言と必ずピットイン文言を設定・試聴できる。通常文言の `{laps}` は予想残り周回数に置き換わり、試聴では5周を使う。必ずピットイン文言は置換せずそのまま読み上げる。

文言は30文字まで。通常文言の `{laps}` 挿入ボタンは、追加後に30文字を超える場合は無効になる。未知の `{...}` は通常文言だけ警告し、入力内容は維持する。空欄では読み上げず、OS標準TTSを利用できない端末では入力と試聴を無効にする。音量0では開始音も本文も試聴しない。

両ソースとも自由文言TTSを使用し、ピットタイミング用の収録WAVは使用しない。試聴の開始音は `ReadoutItemKey.LmuWindows.PitTiming.Root` の設定を共用する。
