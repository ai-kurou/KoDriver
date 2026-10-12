# other-overlay-text-color-detail

その他タブの「オーバーレイ設定」からオーバーレイの文字色をプリセット方式で選択する。
アンバー（既定）、白、赤、緑、青、シアン、マゼンタ、黄の8色から選択する。
色見本と色名、ラジオボタンの選択状態を表示し、アクセシビリティには色名を提供する。
リセットは選択値を `OVERLAY_TEXT_COLOR_DEFAULT` に戻す。OKで `SaveOverlayTextColorUseCase` を通じて
ARGB Longを保存し、キャンセル・ダイアログ外タップでは保存しない。プレビューは行わない。
保存済みのプリセット以外の色は変更せず保持し、スウォッチは未選択として表示する。
JVMの操作・ViewModel・配線・スクリーンショットテストと、実DataStoreを使わないFake moduleを提供する。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-other-overlay-text-color-detail.svg)
<!-- MODULE-GRAPH-END -->
