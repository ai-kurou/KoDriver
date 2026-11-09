package kurou.kodriver.core.speechrecognitiondata.speech

import kurou.kodriver.domain.model.MICROPHONE_SAMPLE_RATE_HZ
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File

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
            ModeSwitchingTranscriber(
                VoskRecognizerAdapter(commandRecognizer),
                VoskRecognizerAdapter(wakeWordRecognizer),
                model::close,
            )
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

/** Vosk の [Recognizer] を [VoskRecognizerPort] として扱う薄いアダプター。 */
internal class VoskRecognizerAdapter(
    private val recognizer: Recognizer,
) : VoskRecognizerPort {
    override fun setMaxAlternatives(count: Int) = recognizer.setMaxAlternatives(count)

    override fun acceptWaveForm(
        pcm: ByteArray,
        size: Int,
    ): Boolean = recognizer.acceptWaveForm(pcm, size)

    override fun result(): String = recognizer.result

    override fun partialResult(): String = recognizer.partialResult

    override fun reset() = recognizer.reset()

    override fun close() = recognizer.close()
}
