package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FlagReadoutTextPreferencesSerializerTest {
    @Test
    fun `デフォルト値は初期設定を返す`() {
        assertEquals(
            FlagReadoutTextPreferences(sectorYellowFlagText = READOUT_CUSTOM_TEXT_DEFAULT),
            FlagReadoutTextPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original = FlagReadoutTextPreferences(sectorYellowFlagText = "イエロー、注意")
            val output = ByteArrayOutputStream()
            FlagReadoutTextPreferencesSerializer.writeTo(original, output)

            val restored = FlagReadoutTextPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

            assertEquals(original, restored)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                FlagReadoutTextPreferencesSerializer.readFrom(corrupt)
            }
        }
}
