package kurou.kodriver.core.speechrecognitiondata.speech

import kurou.kodriver.domain.model.MICROPHONE_SAMPLE_RATE_HZ
import kurou.kodriver.domain.model.SpeechRecognitionMode
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File

/** 確定結果で返してもらう候補の最大数。 */
private const val MAX_ALTERNATIVES = 5

/**
 * Vosk（オフライン音声認識）で [SpeechTranscriber] を作る。日本語モデルは [modelDirectory] に展開しておく。
 */
internal class VoskSpeechTranscriberFactory(
    private val modelDirectory: File,
) : SpeechTranscriberFactory {
    override fun isAvailable(): Boolean = modelDirectory.isDirectory

    override fun create(): SpeechTranscriber {
        useUtf8ForVoskStrings()
        LibVosk.setLogLevel(LogLevel.WARNINGS)
        val model = Model(modelDirectory.absolutePath)
        var commandRecognizer: Recognizer? = null
        var wakeWordRecognizer: Recognizer? = null
        return try {
            commandRecognizer = Recognizer(model, MICROPHONE_SAMPLE_RATE_HZ.toFloat())
            wakeWordRecognizer = Recognizer(model, MICROPHONE_SAMPLE_RATE_HZ.toFloat(), wakeWordGrammar())
            VoskSpeechTranscriber(model, commandRecognizer, wakeWordRecognizer)
        } catch (e: Exception) {
            // 呼び出し側は閉じる対象を受け取れないため、作成済みのネイティブ資源はここで解放する
            wakeWordRecognizer?.close()
            commandRecognizer?.close()
            model.close()
            throw e
        }
    }
}

/** JNAが文字列の変換に使う文字コードを指定するシステムプロパティ名。 */
internal const val JNA_ENCODING_PROPERTY = "jna.encoding"

/**
 * Voskは認識結果のJSONをUTF-8で返すが、JNAは既定でOSの文字コード（Windowsの日本語環境ではMS932）で読むため、
 * 日本語が文字化けして語句に一致しなくなる。Voskのライブラリを読み込む前にUTF-8を指定する。
 */
internal fun useUtf8ForVoskStrings() {
    System.setProperty(JNA_ENCODING_PROPERTY, "UTF-8")
}

internal class VoskSpeechTranscriber(
    private val model: Model,
    private val commandRecognizer: Recognizer,
    private val wakeWordRecognizer: Recognizer,
) : SpeechTranscriber {
    /** 現在の認識に使う Recognizer。読み上げ中は null で、音声を捨てる。 */
    private var active: Recognizer? = wakeWordRecognizer

    init {
        commandRecognizer.setMaxAlternatives(MAX_ALTERNATIVES)
        wakeWordRecognizer.setMaxAlternatives(MAX_ALTERNATIVES)
    }

    override fun accept(pcm: ByteArray): SpeechTranscript? {
        val recognizer = active ?: return null
        return if (recognizer.acceptWaveForm(pcm, pcm.size)) {
            parseVoskFinal(recognizer.result)
        } else {
            parseVoskPartial(recognizer.partialResult)
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
        model.close()
    }
}
