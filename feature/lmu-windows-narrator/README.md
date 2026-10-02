# lmu-windows-narrator

LMUの4種類のフラッグ本文と車両接近開始時の左右文言は、保存した自由文字列をOS標準TTSで読み上げる。
空白文言またはTTS利用不可時は本文を読み上げず、収録WAVへのフォールバックは行わない。
開始音は既存の設定に従う。車両接近の継続時（KeepLeft等）は収録WAVで読み上げる。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-lmu-windows-narrator.svg)
<!-- MODULE-GRAPH-END -->

フラッグ・車両接近開始時のテレメトリログには、読み上げ要求時点の自由文字列を記録する。空欄・TTS利用不可の場合は本文を要求せず、空文字と `SKIPPED` を記録する。`SPOKEN` / `QUEUED` は再生要求の結果であり、非同期発話の完了を保証しない。

`LmuWindowsReadoutTextSpeaker` が各設定の最新文言を取得し、OS標準TTSへ渡す。
車両接近開始時の既定文言は左「カーレフト」、右「カーライト」。旧開始音種別は移行せず、既定文言を使用する。保存時は前後の空白を除去し、30文字までに制限する。
