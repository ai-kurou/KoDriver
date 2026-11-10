# speech-recognition-list

「音声」タブの一覧（listPane）を担う feature モジュール。選択中のシミュレーターについて、「コドライバー」と呼びかけたあとに言うと読み上げられる語句を、読み上げ項目ごとのカードで一覧にする。一覧の先頭には、呼びかけの案内（コドライバー → 確認音が鳴る → 語句 → 読み上げ）を固定で表示する。

- 項目は `ManualReadoutItemCategory`（バーチャルエネルギー・燃料・タイヤ・ブレーキ・ラップ）ごとの見出しでまとめ、カードには項目名と語句をすべて並べる（省略しない）。
- 元データは `ObserveSpeechCommandGuideUseCase`（`:core:domain`）。`SpeechRecognitionListViewModel` が購読して `SpeechRecognitionListUiState` にし、`speechRecognitionListModule` で Koin に登録する。
- 読み上げのオン/オフ・検索・detailPane は対象外。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-speech-recognition-list.svg)
<!-- MODULE-GRAPH-END -->
