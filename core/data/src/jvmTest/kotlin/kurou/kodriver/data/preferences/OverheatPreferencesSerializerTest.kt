package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.OVERHEAT_VOICE_TYPE_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OverheatPreferencesSerializerTest {
    @Test
    fun `デフォルト値は初期設定を返す`() {
        assertEquals(
            OverheatPreferences(voiceType = OVERHEAT_VOICE_TYPE_DEFAULT.id),
            OverheatPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original = OverheatPreferences(voiceType = "standard")
            val output = ByteArrayOutputStream()
            OverheatPreferencesSerializer.writeTo(original, output)

            val restored = OverheatPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

            assertEquals(original, restored)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                OverheatPreferencesSerializer.readFrom(corrupt)
            }
        }
}
