# speech-recognition-list

「音声」タブの一覧（listPane）を担う feature モジュール。選択中のシミュレーターについて、「コドライバー」と呼びかけたあとに言うと読み上げられる語句を、読み上げ項目ごとのカードで一覧にする。一覧の先頭には、呼びかけの案内（「コドライバー」を大きく見せ、確認音 → 語句を言う → 読み上げの3ステップと、長い語句を優先する旨の補足文を添える）を固定で表示する。

- 項目は `ManualReadoutItemCategory`（バーチャルエネルギー・燃料・タイヤ・ブレーキ・ラップ）ごとの見出しでまとめ、カードには手動タブと同じ種類色の帯・アイコンを付け、項目名・語句数・語句をすべて並べる（省略しない）。語句チップは種類色で薄く染める。
- 元データは `ObserveSpeechCommandGuideUseCase`（`:core:domain`）。`SpeechRecognitionListViewModel` が購読して `SpeechRecognitionListUiState` にし、`speechRecognitionListModule` で Koin に登録する。
- 音声認識の語句で依頼された項目が実際に読み上げられたとき（`ObserveManualReadoutItemSpokenUseCase`）、一覧がその項目のカードまで自動でスクロールし（既に全体が見えていればスクロールしない）、決め手になった語句のチップを3回点滅させる。読み上げに至らなかった依頼や、手動タブ・ホットキー由来の読み上げでは何もしない。一覧を表示していない間は購読しないため、別のタブを見ている間の読み上げは無視する。`SpeechRecognitionListUiState.highlight` が強調の指示で、同じ語句を続けても点滅をやり直せるよう読み上げのたびに `sequence` が増える。
- 読み上げのオン/オフ・検索・detailPane は対象外。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-speech-recognition-list.svg)
<!-- MODULE-GRAPH-END -->
