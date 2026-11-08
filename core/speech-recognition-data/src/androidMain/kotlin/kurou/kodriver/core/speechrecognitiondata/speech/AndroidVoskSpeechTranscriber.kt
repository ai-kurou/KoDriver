package kurou.kodriver.core.speechrecognitiondata.speech

import android.content.Context
import android.content.res.AssetManager
import kurou.kodriver.domain.model.MICROPHONE_SAMPLE_RATE_HZ
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File

/** アプリに同梱した日本語モデルの assets 内のディレクトリ名。 */
internal const val VOSK_MODEL_ASSET_DIRECTORY = "vosk-model-ja"

/**
 * assets に同梱した日本語モデルから [SpeechTranscriber] を作る。モデルが同梱されていないビルドでは利用不可になる。
 * Vosk の `Model` は assets を直接読めずディレクトリのパスを要求するため、初回とアプリの更新後に端末の内部ストレージへ展開する
 * （[VoskModelInstaller]。同梱モデルの差し替えが古いモデルのまま残らないよう、アプリの最終更新時刻を version にする）。
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

    private fun installModel(): File {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return VoskModelInstaller(
            target = File(context.filesDir, VOSK_MODEL_ASSET_DIRECTORY),
            version = packageInfo.lastUpdateTime.toString(),
            copyModel = { copyAsset(context.assets, VOSK_MODEL_ASSET_DIRECTORY, it) },
        ).install()
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
