@file:Suppress("FunctionNaming")

package kurou.kodriver.core.texttospeechdata.repository

import io.mockk.confirmVerified
import kotlin.test.Test
import kotlin.test.assertEquals

class VoiceDisplayNameTest {
    @Test
    fun `ローカル音声名を整形する`() {
        assertEquals("日本語 (jab・ローカル)", formatVoiceDisplayName("ja-jp-x-jab-local"))
        confirmVerified()
    }

    @Test
    fun `ネットワーク音声名を整形する`() {
        assertEquals("日本語 (jab・ネットワーク)", formatVoiceDisplayName("ja-jp-x-jab-network"))
        confirmVerified()
    }

    @Test
    fun `その他のvariantはそのまま表示する`() {
        assertEquals("日本語 (jab・custom)", formatVoiceDisplayName("ja-jp-x-jab-custom"))
        confirmVerified()
    }

    @Test
    fun `大文字の音声名も整形する`() {
        assertEquals("日本語 (jab・ローカル)", formatVoiceDisplayName("JA-JP-X-JAB-LOCAL"))
        confirmVerified()
    }

    @Test
    fun `想定外の形式は元の名前を返す`() {
        listOf(
            "",
            "日本語",
            "en-us-x-jab-local",
            "ja-jp-x-jab",
            "ja-jp-x--local",
            "ja-jp-x-jab-",
            "ja-jp-x-jab-local-extra",
        ).forEach { name -> assertEquals(name, formatVoiceDisplayName(name)) }
        confirmVerified()
    }
}
