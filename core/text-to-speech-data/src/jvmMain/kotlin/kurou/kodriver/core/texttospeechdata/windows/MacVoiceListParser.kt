package kurou.kodriver.core.texttospeechdata.windows

import kurou.kodriver.domain.model.TTS_CULTURE_NAME
import kurou.kodriver.domain.model.TextToSpeechVoice

private val MAC_VOICE_LINE = Regex("""^(.+?)\s+([A-Za-z]{2,3}_[A-Za-z0-9]+)\s+#.*$""")

/** 音声が未指定のときに使う日本語音声。音声一覧では既定として扱う。 */
internal const val MAC_DEFAULT_VOICE = "Kyoko"

/**
 * macの`say -v ?`の出力を解析し、読み上げ言語（[TTS_CULTURE_NAME]）に一致する音声だけを返す。
 * 行は`名前    ja_JP    # サンプル文`の形式で、名前は括弧付き（例: `Eddy (日本語（日本）)`）の場合もある。
 * 名前をそのままIDと表示名にし、ロケールの`_`を`-`に置き換えて言語名とする。形式に合わない行は除外する。
 */
internal fun parseMacVoiceList(output: String): List<TextToSpeechVoice> =
    output
        .lineSequence()
        .mapNotNull { MAC_VOICE_LINE.matchEntire(it.trim()) }
        .map { match ->
            val name = match.groupValues[1].trim()
            TextToSpeechVoice(name, name, match.groupValues[2].replace('_', '-'), isDefault = name == MAC_DEFAULT_VOICE)
        }.filter { it.cultureName == TTS_CULTURE_NAME }
        .toList()
