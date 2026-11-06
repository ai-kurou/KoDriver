package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
            .mapNotNull { alternative ->
                val fields = alternative.jsonObject
                val text = fields["text"]?.jsonPrimitive?.contentOrNull?.replace(" ", "")
                val confidence = fields["confidence"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                text?.takeIf { it.isNotEmpty() }?.let { it to confidence }
            }.sortedByDescending { (_, confidence) -> confidence }
            .map { (text, _) -> text }
            .distinct()
    return candidates.takeIf { it.isNotEmpty() }?.let(SpeechTranscript::Final)
}
