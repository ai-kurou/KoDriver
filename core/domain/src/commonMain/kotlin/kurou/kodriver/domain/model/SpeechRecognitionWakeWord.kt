package kurou.kodriver.domain.model

/**
 * ウェイクワード「コドライバー」。認識モデルの語彙の表記（ひらがな・カタカナ）がどちらか、「コ」が「この」に分かれるかは
 * 実機でしか分からないため、それぞれを持つ。空白は認識結果の側で除去されている。
 *
 * 単独の「ドライバー」は、実況や会話など普通の発話にも出て誤って反応するため含めない。
 */
val SPEECH_RECOGNITION_WAKE_WORDS: List<String> =
    listOf("コドライバー", "こどらいばー", "このドライバー", "このどらいばー")

/** 認識結果の候補のどれかにウェイクワードが含まれるか。 */
fun containsSpeechRecognitionWakeWord(candidates: List<String>): Boolean =
    candidates.any { candidate -> SPEECH_RECOGNITION_WAKE_WORDS.any { candidate.contains(it) } }
