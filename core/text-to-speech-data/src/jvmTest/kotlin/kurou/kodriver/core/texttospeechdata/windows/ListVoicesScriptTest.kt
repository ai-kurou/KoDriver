package kurou.kodriver.core.texttospeechdata.windows

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ListVoicesScriptTest {
    @Test
    fun `Windowsのコマンドラインで欠落する二重引用符を含まない`() {
        assertFalse(buildListVoicesScript().contains('"'))
    }

    @Test
    fun `有効なSAPI音声の情報をUTF8のタブ区切りで出力する`() {
        val script = buildListVoicesScript()

        assertTrue(script.contains("[Console]::OutputEncoding = [System.Text.Encoding]::UTF8;"))
        assertTrue(script.contains("Add-Type -AssemblyName System.Speech;"))
        assertTrue(script.contains("GetInstalledVoices()"))
        assertTrue(script.contains("Where-Object { \$_.Enabled }"))
        assertTrue(script.contains("\$v = \$_.VoiceInfo;"))
        assertTrue(
            script.contains(
                "[Console]::WriteLine([string]::Join([string][char]9, @(\$v.Name, \$v.Description, \$v.Culture.Name)))",
            ),
        )
    }
}
