package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LmuWindowsTyreWearPreferencesSerializerTest {
    @Test
    fun `デフォルト値は閾値30と既定文言`() {
        assertEquals(
            LmuWindowsTyreWearPreferences(
                thresholdPercentage = LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT,
                readoutText = LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT,
            ),
            LmuWindowsTyreWearPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original =
                LmuWindowsTyreWearPreferences(thresholdPercentage = 50, readoutText = "残量{percent}%")
            val output = ByteArrayOutputStream()
            LmuWindowsTyreWearPreferencesSerializer.writeTo(original, output)

            val restored =
                LmuWindowsTyreWearPreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                )

            assertEquals(original, restored)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                LmuWindowsTyreWearPreferencesSerializer.readFrom(corrupt)
            }
        }

    @Test
    fun `文言フィールドがない旧データは既定文言を使用する`() =
        runTest {
            val restored =
                LmuWindowsTyreWearPreferencesSerializer.readFrom(
                    ByteArrayInputStream(byteArrayOf(0x08, 0x32)),
                )

            assertEquals(50, restored.thresholdPercentage)
            assertEquals(LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT, restored.readoutText)
        }

    @Test
    fun `保存した空欄文言は読み出しても既定値に戻らない`() =
        runTest {
            val original = LmuWindowsTyreWearPreferences(readoutText = "")
            val output = ByteArrayOutputStream()
            LmuWindowsTyreWearPreferencesSerializer.writeTo(original, output)

            assertEquals(
                original,
                LmuWindowsTyreWearPreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                ),
            )
        }
}
