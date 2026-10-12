# windows-hotkey-data

Windowsグローバルホットキーを検出するJVM専用のI/O基盤です。
`GlobalHotkey` で修飾キーと仮想キーコード（F13 は `GlobalHotkey.VK_F13`）を指定し、
`WindowsGlobalHotkeySource.create()` が返す `GlobalHotkeySource.observe()` から押下ごとに `Unit` を受け取ります。
非Windows環境ではネイティブAPIを読み込まず、Flowは通知せず即座に完了します。
`HotkeyRepositoryImpl` は修飾キーなしの F9（`GlobalHotkey.VK_F9 = 0x78`）を固定で監視します。
`windowsHotkeyDataModule` がドメインの `HotkeyRepository` をデスクトップアプリに提供します。
SimHub Control Mapper から F9 を送ると、LMU の残りバーチャルエナジー現在値を読み上げます。

Flowの購読中だけ専用メッセージループスレッドで `RegisterHotKey` / `GetMessage` を実行します。
`MOD_NOREPEAT` で長押しによる連続通知を防ぎます。登録に失敗した場合はSentryへ記録し、通知せず完了します。
キャンセル時はキューの準備を待って `PostThreadMessage(WM_QUIT)` を呼び、登録したスレッドで
`UnregisterHotKey` を実行してからスレッドの終了を待ちます。

`JnaHotkeyNativeApi` はプラットフォーム固有の外部APIを直接呼び出すため、ユニットテストとカバレッジの対象外です。
上位の登録・通知・解除・OS分岐は `HotkeyNativeApi` のFakeでテストします。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/core-windows-hotkey-data.svg)
<!-- MODULE-GRAPH-END -->
