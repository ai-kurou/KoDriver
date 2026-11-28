# other-wake-word-hotkey-detail

その他タブのマイク設定「ウェイクワードホットキー」の詳細ペイン。タイトルと説明に加えて、Windows デスクトップ版では「ホットキー」カードを表示する（Android・macOS・Linux では説明のみで、`ViewModel` も作らない）。ホットキーの押下は `:feature:main` の `SpeechRecognitionViewModel` が受け取り、コマンドの受付を始める。

- 数字0–9または英字A–Zと、修飾キー（Ctrl + Alt（初期値）/ Ctrl + Shift / Alt + Shift）を組み合わせて割り当てる。キー未割り当てが OFF にあたる。
- 全シミュレーターの手動読み上げセットと同じ「修飾キー＋キー」は保存せず、使用中のシミュレーターとセットを表示する。手動読み上げ側の重複確認は `:feature:manual-readout-detail` が行う。
- 割り当て済みで音声認識が OFF の間は「音声認識がOFFのため、押しても反応しません」と表示する（ON/OFF は `ObserveSpeechRecognitionEnabledUseCase` で購読する）。
- 保存済みキーがある場合は登録状態を表示し、競合・失敗時は「再試行」を提供する。OS への登録は、押下を購読する `:feature:main` によりアプリの起動中は維持される。
- カード末尾のリンクから [SimHub手順書](https://github.com/ai-kurou/KoDriver/blob/main/docs/simhub-hotkey-setup.md) を開く。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-other-wake-word-hotkey-detail.svg)
<!-- MODULE-GRAPH-END -->
