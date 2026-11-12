package kurou.kodriver.domain.util

/**
 * 音声認識（Vosk）が実機でよく取り違える語と、その正しい語。手動読み上げ項目の語句と照合する前に、認識結果の中の前者を後者へ置き換える。
 * 画面の案内には出さない。置換元は空白を除いた小文字の表記で書く。上から順に適用する。
 * 置換元は、他の発話に紛れ込みにくい長さのものだけを登録する（短すぎる語は誤爆しやすい）。
 */
internal val SPEECH_RECOGNITION_CORRECTIONS: List<Pair<String, String>> =
    listOf(
        "産業" to "残量",
        "集会" to "周回",
        "まーも" to "摩耗",
        "ポンド" to "温度",
    )
