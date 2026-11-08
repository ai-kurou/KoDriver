package kurou.kodriver.domain.model

/**
 * ウェイクワード。認識モデルの語彙の表記（ひらがな・カタカナ）がどちらか実機でしか分からないため、両方を持つ。
 * 空白は認識結果の側で除去されている。
 */
val SPEECH_RECOGNITION_WAKE_WORDS: List<String> =
    listOf("このドライバー", "このどらいばー", "ドライバー", "どらいばー")

/** 認識結果の候補のどれかにウェイクワードが含まれるか。 */
fun containsSpeechRecognitionWakeWord(candidates: List<String>): Boolean =
    candidates.any { candidate -> SPEECH_RECOGNITION_WAKE_WORDS.any { candidate.contains(it) } }
