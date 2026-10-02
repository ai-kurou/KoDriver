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
            "try { \$s.SelectVoice('${voiceId.replace("'", "''")}') } catch { $fallback }; "
        }
    return "Add-Type -AssemblyName System.Speech; " +
        "\$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
        "\$s.Volume = $volume; " +
        selection +
        "\$s.Speak('${text.replace("'", "''")}')"
}
