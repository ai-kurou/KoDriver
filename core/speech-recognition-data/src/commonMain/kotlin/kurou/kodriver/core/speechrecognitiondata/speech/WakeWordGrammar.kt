package kurou.kodriver.core.speechrecognitiondata.speech

import kurou.kodriver.domain.model.SPEECH_RECOGNITION_WAKE_WORDS

/** ウェイクワード以外の音を受け止める、Voskの文法上の未知語。これが無いと別の語がウェイクワードに誤認される。 */
private const val VOSK_UNKNOWN_WORD = "[unk]"

/**
 * ウェイクワードに語彙を絞るVoskの文法（JSONの文字列配列）。
 * モデルの語彙に無い語は無視されるため、ひらがな・カタカナの両方を含める。
 */
internal fun wakeWordGrammar(words: List<String> = SPEECH_RECOGNITION_WAKE_WORDS): String =
    (words + VOSK_UNKNOWN_WORD).joinToString(separator = ",", prefix = "[", postfix = "]") { "\"$it\"" }
