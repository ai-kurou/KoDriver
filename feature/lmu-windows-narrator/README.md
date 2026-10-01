# lmu-windows-narrator

LMUの4種類のフラッグ本文は、保存した自由文字列をOS標準TTSで読み上げる。
空白文言またはTTS利用不可時は本文を読み上げず、収録WAVへのフォールバックは行わない。
開始音は既存の設定に従う。フラッグ以外の収録音声には影響しない。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-lmu-windows-narrator.svg)
<!-- MODULE-GRAPH-END -->

フラッグのテレメトリログには、読み上げ要求時点の自由文字列を記録する。空欄・TTS利用不可の場合は本文を要求せず、空文字と `SKIPPED` を記録する。`SPOKEN` / `QUEUED` は再生要求の結果であり、非同期発話の完了を保証しない。
