package kurou.kodriver.core.speechrecognitiondata.speech

import kurou.kodriver.domain.model.SpeechRecognitionMode

/** Vosk の `Recognizer` のうち [ModeSwitchingTranscriber] が使うものだけを取り出した窓口。Desktop と Android で `Recognizer` の実体が別なため分ける。 */
internal interface VoskRecognizerPort : AutoCloseable {
    /** 確定結果に含める候補の最大数を指定する。 */
    fun setMaxAlternatives(count: Int)

    /** 音声を渡す。発話が区切れて確定結果が得られたら true。 */
    fun acceptWaveForm(
        pcm: ByteArray,
        size: Int,
    ): Boolean

    /** 直近の確定結果の JSON。 */
    fun result(): String

    /** 発話途中の結果の JSON。 */
    fun partialResult(): String

    /** 認識中の途中経過や音声を捨てる。 */
    fun reset()
}

/** 確定結果に含める信頼度順の候補の最大数。 */
internal const val MAX_ALTERNATIVES = 5

/**
 * 語彙をウェイクワードに絞った認識器（ウェイクワード待ち）と絞らない認識器（コマンド待ち）を [SpeechRecognitionMode] で切り替える、
 * Desktop と Android 共通の [SpeechTranscriber]。[SpeechRecognitionMode.Paused] の間は音声を捨てる。
 *
 * [closeModel] は 2 つの認識器を閉じたあとに呼ぶ（認識器は読み込み済みのモデルを使うため、モデルを最後に閉じる）。
 */
internal class ModeSwitchingTranscriber(
    private val commandRecognizer: VoskRecognizerPort,
    private val wakeWordRecognizer: VoskRecognizerPort,
    private val closeModel: () -> Unit,
) : SpeechTranscriber {
    /** 現在の認識に使う認識器。読み上げ中は null で、音声を捨てる。 */
    private var active: VoskRecognizerPort? = wakeWordRecognizer

    init {
        commandRecognizer.setMaxAlternatives(MAX_ALTERNATIVES)
        wakeWordRecognizer.setMaxAlternatives(MAX_ALTERNATIVES)
    }

    override fun accept(pcm: ByteArray): SpeechTranscript? {
        val recognizer = active ?: return null
        return if (recognizer.acceptWaveForm(pcm, pcm.size)) {
            parseVoskFinal(recognizer.result())
        } else {
            parseVoskPartial(recognizer.partialResult())
        }
    }

    override fun setMode(mode: SpeechRecognitionMode) {
        active =
            when (mode) {
                SpeechRecognitionMode.WakeWord -> wakeWordRecognizer
                SpeechRecognitionMode.Command -> commandRecognizer
                SpeechRecognitionMode.Paused -> null
            }
        // 切り替え前の途中経過や音声を引き継がない
        active?.reset()
    }

    override fun close() {
        wakeWordRecognizer.close()
        commandRecognizer.close()
        closeModel()
    }
}
