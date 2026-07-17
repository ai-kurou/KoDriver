package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.model.RedFlagPreferences
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RedFlagPreferencesSerializerTest {

    @Test
    fun `デフォルト値は初期設定を返す`() {
        assertEquals(
            RedFlagPreferences(voiceType = "session_stop"),
            RedFlagPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() = runTest {
        val original = RedFlagPreferences(voiceType = "red_flag")
        val output = ByteArrayOutputStream()
        RedFlagPreferencesSerializer.writeTo(original, output)

        val restored = RedFlagPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

        assertEquals(original, restored)
    }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() = runTest {
        val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

        assertFailsWith<CorruptionException> {
            RedFlagPreferencesSerializer.readFrom(corrupt)
        }
    }
}
