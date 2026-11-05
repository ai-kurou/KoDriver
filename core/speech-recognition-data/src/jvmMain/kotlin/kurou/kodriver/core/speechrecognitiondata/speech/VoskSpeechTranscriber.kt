package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kurou.kodriver.domain.model.MICROPHONE_SAMPLE_RATE_HZ
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
        LibVosk.setLogLevel(LogLevel.WARNINGS)
        val model = Model(modelDirectory.absolutePath)
        return try {
            VoskSpeechTranscriber(model, Recognizer(model, MICROPHONE_SAMPLE_RATE_HZ.toFloat()))
        } catch (e: Exception) {
            model.close()
            throw e
        }
    }
}

internal class VoskSpeechTranscriber(
    private val model: Model,
    private val recognizer: Recognizer,
) : SpeechTranscriber {
    init {
        recognizer.setMaxAlternatives(MAX_ALTERNATIVES)
    }

    override fun accept(pcm: ByteArray): SpeechTranscript? =
        if (recognizer.acceptWaveForm(pcm, pcm.size)) {
            parseVoskFinal(recognizer.result)
        } else {
            parseVoskPartial(recognizer.partialResult)
        }

    override fun close() {
        recognizer.close()
        model.close()
    }
}

/** `{"partial":"..."}` から途中経過を取り出す。空文字なら null。 */
internal fun parseVoskPartial(json: String): SpeechTranscript.Partial? =
    Json
        .parseToJsonElement(json)
        .jsonObject["partial"]
        ?.jsonPrimitive
        ?.contentOrNull
        ?.replace(" ", "")
        ?.takeIf { it.isNotEmpty() }
        ?.let(SpeechTranscript::Partial)

/**
 * `{"alternatives":[{"text":"...","confidence":..},..]}` から候補を信頼度の高い順に取り出す。
 * 候補が無い・空文字だけのときは null。Voskの日本語モデルは単語を空白で区切るため、空白は取り除く。
 */
internal fun parseVoskFinal(json: String): SpeechTranscript.Final? {
    val candidates =
        Json
            .parseToJsonElement(json)
            .jsonObject["alternatives"]
            ?.jsonArray
            .orEmpty()
            .mapNotNull { it.jsonObject["text"]?.jsonPrimitive?.contentOrNull }
            .map { it.replace(" ", "") }
            .filter { it.isNotEmpty() }
            .distinct()
    return candidates.takeIf { it.isNotEmpty() }?.let(SpeechTranscript::Final)
}
