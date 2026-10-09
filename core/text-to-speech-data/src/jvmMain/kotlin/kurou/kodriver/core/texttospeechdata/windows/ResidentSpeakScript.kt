package kurou.kodriver.core.texttospeechdata.windows

import kurou.kodriver.domain.model.TTS_CULTURE_NAME
import java.util.Base64

/** 常駐スクリプトが読み上げ完了（または中断）ごとに標準出力へ書く応答行。 */
internal const val SPEAK_RESPONSE_DONE = "DONE"

/** 実行中の読み上げを打ち切る要求行。 */
internal const val SPEAK_REQUEST_STOP = "STOP"

/**
 * 常駐PowerShellプロセスで実行するスクリプトを組み立てる。
 *
 * `SpeechSynthesizer` を1度だけ生成して使い回し、標準入力から次の要求行を受け取る。
 * - `SPEAK <音量> <音声IDのBase64> <テキストのBase64>`: 読み上げて、完了後に [SPEAK_RESPONSE_DONE] を出力する。
 *   音声IDが空なら日本語音声を選び、指定音声が選べない場合も日本語音声へフォールバックする。
 * - [SPEAK_REQUEST_STOP]: 読み上げ中なら打ち切る（打ち切られた読み上げも [SPEAK_RESPONSE_DONE] を出力する）。
 *   読み上げ中でなければ無視する。
 * 標準入力が閉じられたら終了する。
 *
 * 読み上げ中も次の要求行を読めるよう `SpeakAsync` と非同期の行読み込みを併用し、
 * 完了は `SpeakCompleted` イベントを `Start-Sleep` で間隔を空けて確認して検知する（`Wait-Event -Timeout` は整数秒のため使わない）。要求行はBase64でASCIIに限定し、
 * エスケープ処理を不要にしている。コマンドライン引数の引用符欠落を避けるため、
 * スクリプトは `-EncodedCommand` で渡す（[encodeResidentSpeakScript]）。
 */
internal fun buildResidentSpeakScript(): String =
    """
    Add-Type -AssemblyName System.Speech
    ${'$'}s = New-Object System.Speech.Synthesis.SpeechSynthesizer
    Register-ObjectEvent -InputObject ${'$'}s -EventName SpeakCompleted -SourceIdentifier KoDriverSpeakDone | Out-Null
    ${'$'}in = New-Object System.IO.StreamReader([Console]::OpenStandardInput(), [System.Text.Encoding]::ASCII)
    ${'$'}utf8 = [System.Text.Encoding]::UTF8
    function Select-JaVoice {
        ${'$'}s.SelectVoiceByHints([System.Speech.Synthesis.VoiceGender]::NotSet, [System.Speech.Synthesis.VoiceAge]::NotSet, 0, [System.Globalization.CultureInfo]::GetCultureInfo('$TTS_CULTURE_NAME'))
    }
    ${'$'}pending = ${'$'}null
    while (${'$'}true) {
        if (${'$'}pending -eq ${'$'}null) { ${'$'}pending = ${'$'}in.ReadLineAsync() }
        ${'$'}pending.Wait()
        ${'$'}line = ${'$'}pending.Result
        ${'$'}pending = ${'$'}null
        if (${'$'}line -eq ${'$'}null) { break }
        if (-not ${'$'}line.StartsWith('SPEAK ')) { continue }
        ${'$'}eof = ${'$'}false
        try {
            ${'$'}parts = ${'$'}line.Split(' ')
            ${'$'}s.Volume = [int]${'$'}parts[1]
            ${'$'}voiceId = ${'$'}utf8.GetString([Convert]::FromBase64String(${'$'}parts[2]))
            ${'$'}text = ${'$'}utf8.GetString([Convert]::FromBase64String(${'$'}parts[3]))
            if (${'$'}voiceId.Length -eq 0) { Select-JaVoice } else { try { ${'$'}s.SelectVoice(${'$'}voiceId) } catch { Select-JaVoice } }
            Get-Event -SourceIdentifier KoDriverSpeakDone -ErrorAction SilentlyContinue | Remove-Event
            ${'$'}s.SpeakAsync(${'$'}text) | Out-Null
            ${'$'}pending = ${'$'}in.ReadLineAsync()
            ${'$'}completed = ${'$'}false
            while (-not ${'$'}completed -and -not ${'$'}pending.IsCompleted) {
                ${'$'}e = Get-Event -SourceIdentifier KoDriverSpeakDone -ErrorAction SilentlyContinue
                if (${'$'}e) { ${'$'}e | Remove-Event; ${'$'}completed = ${'$'}true } else { Start-Sleep -Milliseconds 20 }
            }
            if (-not ${'$'}completed) {
                ${'$'}eof = ${'$'}pending.Result -eq ${'$'}null
                ${'$'}pending = ${'$'}null
                ${'$'}s.SpeakAsyncCancelAll()
                ${'$'}e = Wait-Event -SourceIdentifier KoDriverSpeakDone -Timeout 2
                if (${'$'}e) { Remove-Event -EventIdentifier ${'$'}e.EventIdentifier }
            }
        } catch { }
        [Console]::Out.WriteLine('$SPEAK_RESPONSE_DONE')
        [Console]::Out.Flush()
        if (${'$'}eof) { break }
    }
    """.trimIndent()

/** `powershell.exe -EncodedCommand` に渡す、UTF-16LEのBase64へ変換したスクリプト。 */
internal fun encodeResidentSpeakScript(script: String = buildResidentSpeakScript()): String =
    Base64.getEncoder().encodeToString(script.toByteArray(Charsets.UTF_16LE))

/** 常駐スクリプトへ送る `SPEAK` 要求行（改行なし）を組み立てる。 */
internal fun buildSpeakRequest(
    text: String,
    volume: Int,
    voiceId: String,
): String {
    val encoder = Base64.getEncoder()
    return "SPEAK $volume " +
        encoder.encodeToString(voiceId.toByteArray(Charsets.UTF_8)) + " " +
        encoder.encodeToString(text.toByteArray(Charsets.UTF_8))
}
