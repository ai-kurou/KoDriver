package kurou.kodriver.core.texttospeechdata.windows

import kurou.kodriver.domain.model.TTS_CULTURE_NAME

/**
 * 有効なSAPI音声の名前・説明・言語・既定フラグをUTF-8のタブ区切りで出力するスクリプトを組み立てる。
 * Windowsのコマンドライン引数で二重引用符が欠落するため、タブは文字コードで指定する。
 */
internal fun buildListVoicesScript(): String =
    "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; " +
        "Add-Type -AssemblyName System.Speech; " +
        "\$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
        "\$s.SelectVoiceByHints([System.Speech.Synthesis.VoiceGender]::NotSet, " +
        "[System.Speech.Synthesis.VoiceAge]::NotSet, 0, " +
        "[System.Globalization.CultureInfo]::GetCultureInfo('$TTS_CULTURE_NAME')); " +
        "\$defaultName = \$s.Voice.Name; " +
        "\$s.GetInstalledVoices() | " +
        "Where-Object { \$_.Enabled } | ForEach-Object { " +
        "\$v = \$_.VoiceInfo; " +
        "[Console]::WriteLine([string]::Join([string][char]9, " +
        "@(\$v.Name, \$v.Description, \$v.Culture.Name, (\$v.Name -eq \$defaultName)))) }"
