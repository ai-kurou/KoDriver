package kurou.kodriver.core.speechrecognitiondata.speech

import kurou.kodriver.domain.model.SPEECH_RECOGNITION_WAKE_WORDS
import kurou.kodriver.domain.model.SPEECH_RECOGNITION_WAKE_WORD_GRAMMAR_PHRASES

/** ウェイクワード以外の音を受け止める、Voskの文法上の未知語。これが無いと別の語がウェイクワードに誤認される。 */
private const val VOSK_UNKNOWN_WORD = "[unk]"

/**
 * ウェイクワードに語彙を絞るVoskの文法（JSONの文字列配列）。
 * モデルの語彙に無い語は無視されるため、語彙にある語(空白で区切った句を含む)だけを渡す。
 */
internal fun wakeWordGrammar(
    words: List<String> = SPEECH_RECOGNITION_WAKE_WORDS + SPEECH_RECOGNITION_WAKE_WORD_GRAMMAR_PHRASES,
): String = (words + VOSK_UNKNOWN_WORD).joinToString(separator = ",", prefix = "[", postfix = "]") { "\"$it\"" }
