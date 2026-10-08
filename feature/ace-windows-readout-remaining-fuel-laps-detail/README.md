# ace-windows-readout-remaining-fuel-laps-detail

ACEが算出する残燃料で走行可能な周回数を読み上げる機能の詳細設定画面。残り周回数の閾値（1〜5周、デフォルト3周）と有効スイッチを設定する。判定タイミングと判定ロジックは従来どおり。

「1周以上のときの文言」（デフォルト `燃料は残り約{laps}周`）と「燃料がありません(0周以下)のときの文言」（デフォルト `燃料がありません`）を個別に設定できる。各欄は `READOUT_CUSTOM_TEXT_MAX_LENGTH` 文字までで、保存時に前後の空白を除去し DataStore に永続化する。1周以上用は挿入チップで `{laps}` を追加でき、未知プレースホルダーには警告を表示する。燃料なし用にはプレースホルダー機能を設けない。

試聴は `ReadoutItemKey.AceWindows.RemainingFuelLaps.Root` の開始音の後にOS標準TTSで入力文言を再生する。1周以上用の `{laps}` は現在のスライダー閾値に置換し、燃料なし用はそのまま再生する。有効スイッチがOFFでも編集・試聴できる。空白文言・TTS利用不可では読み上げず、WAVにはフォールバックしない。試聴時の音量がゼロ以下の場合も開始音・本文を再生しない。

各文言の「デフォルトに戻す」ボタンで既定文言に戻せる（TTS利用不可、または既にデフォルトと同じ場合は無効）。TTS利用不可では入力・試聴・挿入も無効にし、説明を表示する。保存が非同期でも入力中の最新文言を古い保存値で巻き戻さない。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-ace-windows-readout-remaining-fuel-laps-detail.svg)
<!-- MODULE-GRAPH-END -->
