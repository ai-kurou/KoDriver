package kurou.kodriver.core.speechrecognitiondata.speech

import android.content.Context
import android.content.res.AssetManager
import kurou.kodriver.domain.model.MICROPHONE_SAMPLE_RATE_HZ
import kurou.kodriver.domain.model.SpeechRecognitionMode
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File

/** アプリに同梱した日本語モデルの assets 内のディレクトリ名。 */
internal const val VOSK_MODEL_ASSET_DIRECTORY = "vosk-model-ja"

/** 確定結果で返してもらう候補の最大数。 */
private const val MAX_ALTERNATIVES = 5

/** モデルの展開が最後まで終わったことを示す、展開先に置くファイル名。 */
private const val MODEL_READY_MARKER = ".ready"

/**
 * assets に同梱した日本語モデルから [SpeechTranscriber] を作る。モデルが同梱されていないビルドでは利用不可になる。
 * Vosk の `Model` は assets を直接読めずディレクトリのパスを要求するため、初回に端末の内部ストレージへ展開する。
 * プラットフォームのAPIを直接呼ぶ薄いアダプターのため、単体テストの対象にしない。
 */
internal class AndroidVoskSpeechTranscriberFactory(
    private val context: Context,
) : SpeechTranscriberFactory {
    override fun isAvailable(): Boolean =
        context.assets
            .list(VOSK_MODEL_ASSET_DIRECTORY)
            .orEmpty()
            .isNotEmpty()

    override fun create(): SpeechTranscriber {
        val directory = installModel()
        LibVosk.setLogLevel(LogLevel.WARNINGS)
        val model = Model(directory.absolutePath)
        var commandRecognizer: Recognizer? = null
        var wakeWordRecognizer: Recognizer? = null
        return try {
            commandRecognizer = Recognizer(model, MICROPHONE_SAMPLE_RATE_HZ.toFloat())
            wakeWordRecognizer = Recognizer(model, MICROPHONE_SAMPLE_RATE_HZ.toFloat(), wakeWordGrammar())
            AndroidVoskSpeechTranscriber(model, commandRecognizer, wakeWordRecognizer)
        } catch (e: Exception) {
            // 呼び出し側は閉じる対象を受け取れないため、作成済みのネイティブ資源はここで解放する
            wakeWordRecognizer?.close()
            commandRecognizer?.close()
            model.close()
            throw e
        }
    }

    private fun installModel(): File {
        val target = File(context.filesDir, VOSK_MODEL_ASSET_DIRECTORY)
        val marker = File(target, MODEL_READY_MARKER)
        if (!marker.exists()) {
            target.deleteRecursively()
            copyAsset(context.assets, VOSK_MODEL_ASSET_DIRECTORY, target)
            marker.createNewFile()
        }
        return target
    }

    private fun copyAsset(
        assets: AssetManager,
        path: String,
        target: File,
    ) {
        val children = assets.list(path).orEmpty()
        if (children.isEmpty()) {
            assets.open(path).use { input -> target.outputStream().use { output -> input.copyTo(output) } }
        } else {
            target.mkdirs()
            children.forEach { copyAsset(assets, "$path/$it", File(target, it)) }
        }
    }
}

internal class AndroidVoskSpeechTranscriber(
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
