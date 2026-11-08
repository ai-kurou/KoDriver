package kurou.kodriver.domain.model

/**
 * ウェイクワード。認識モデル(vosk-model-small-ja)の語彙には「コドライバー」「このドライバー」やひらがなの表記が無いため、
 * 語彙にある「ドライバー」の表記ゆれ(「ドライバ」「ドライヴァー」)で検出する。「コドライバー」「このドライバー」は
 * 「ドライバー」を含むため、これらでも検出できる。空白は認識結果の側で除去されている。
 */
val SPEECH_RECOGNITION_WAKE_WORDS: List<String> =
    listOf("ドライバー", "ドライバ", "ドライヴァー")

/**
 * ウェイクワード待ちの文法に加える、語彙の語を空白で区切った句。「コドライバー」「このドライバー」はモデルの語彙に
 * 1語として無いため、語彙にある「コ」「この」と「ドライバー」に分けて渡す。
 */
val SPEECH_RECOGNITION_WAKE_WORD_GRAMMAR_PHRASES: List<String> =
    listOf("コ ドライバー", "この ドライバー")

/** 認識結果の候補のどれかにウェイクワードが含まれるか。 */
fun containsSpeechRecognitionWakeWord(candidates: List<String>): Boolean =
    candidates.any { candidate -> SPEECH_RECOGNITION_WAKE_WORDS.any { candidate.contains(it) } }
