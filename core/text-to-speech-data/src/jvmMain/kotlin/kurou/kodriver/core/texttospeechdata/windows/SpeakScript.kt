package kurou.kodriver.core.texttospeechdata.windows

import kurou.kodriver.domain.model.TTS_CULTURE_NAME

/** 音声IDとテキストをエスケープし、指定音声が使えなければ日本語音声で読み上げるスクリプトを組み立てる。 */
internal fun buildSpeakScript(
    text: String,
    volume: Int,
    voiceId: String,
): String {
    val fallback =
        "\$s.SelectVoiceByHints(" +
            "[System.Speech.Synthesis.VoiceGender]::NotSet, " +
            "[System.Speech.Synthesis.VoiceAge]::NotSet, 0, " +
            "[System.Globalization.CultureInfo]::GetCultureInfo('$TTS_CULTURE_NAME'))"
    val selection =
        if (voiceId.isEmpty()) {
            "$fallback; "
        } else {
            "try { \$s.SelectVoice('${escapeSingleQuoted(voiceId)}') } catch { $fallback }; "
        }
    return "Add-Type -AssemblyName System.Speech; " +
        "\$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
        "\$s.Volume = $volume; " +
        selection +
        "\$s.Speak('${escapeSingleQuoted(text)}')"
}

/**
 * PowerShellの単一引用符文字列へ埋め込むため、引用符として扱われる文字を二重化する。
 * PowerShellは`'`だけでなく、タイポグラフィ引用符（U+2018〜U+201B）も単一引用符とみなす。
 */
private fun escapeSingleQuoted(value: String): String = SINGLE_QUOTE_REGEX.replace(value) { it.value + it.value }

private val SINGLE_QUOTE_REGEX = Regex("['\u2018\u2019\u201A\u201B]")
