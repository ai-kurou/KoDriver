package kurou.kodriver.core.texttospeechdata.windows

/**
 * 有効なSAPI音声の名前・説明・言語をUTF-8のタブ区切りで出力するスクリプトを組み立てる。
 * Windowsのコマンドライン引数で二重引用符が欠落するため、タブは文字コードで指定する。
 */
internal fun buildListVoicesScript(): String =
    "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; " +
        "Add-Type -AssemblyName System.Speech; " +
        "(New-Object System.Speech.Synthesis.SpeechSynthesizer).GetInstalledVoices() | " +
        "Where-Object { \$_.Enabled } | ForEach-Object { " +
        "\$v = \$_.VoiceInfo; " +
        "[Console]::WriteLine([string]::Join([string][char]9, @(\$v.Name, \$v.Description, \$v.Culture.Name))) }"
