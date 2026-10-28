# main

AppScreen の設定・接続状態に加え、端末または KoDriver の音量低下警告を監視する `LowVolumeWarningViewModel` を提供します。警告を閉じた状態は ViewModel のメモリにのみ保持し、音量が閾値以上に戻る（警告が解消する）までバナーを再表示しません。音量詳細設定から警告バナーをオフにできます。

AppScreen の UI 状態の合成は `AppScreenFlow` が cold Flow として担います。共有・初期値・`viewModelScope` での実行は `AppScreenViewModel` が管理します。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-main.svg)
<!-- MODULE-GRAPH-END -->
