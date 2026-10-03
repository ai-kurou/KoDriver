package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LmuWindowsRemainingVirtualEnergyPreferencesSerializerTest {
    @Test
    fun `デフォルト値は閾値30と既定文言`() {
        assertEquals(
            LmuWindowsRemainingVirtualEnergyPreferences(
                thresholdPercentage = 30,
                readoutText = LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT,
            ),
            LmuWindowsRemainingVirtualEnergyPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original =
                LmuWindowsRemainingVirtualEnergyPreferences(thresholdPercentage = 50, readoutText = "残量{percent}%")
            val output = ByteArrayOutputStream()
            LmuWindowsRemainingVirtualEnergyPreferencesSerializer.writeTo(original, output)

            val restored =
                LmuWindowsRemainingVirtualEnergyPreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                )

            assertEquals(original, restored)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                LmuWindowsRemainingVirtualEnergyPreferencesSerializer.readFrom(corrupt)
            }
        }

    @Test
    fun `文言フィールドがない旧データは既定文言を使用する`() =
        runTest {
            val restored =
                LmuWindowsRemainingVirtualEnergyPreferencesSerializer.readFrom(
                    ByteArrayInputStream(byteArrayOf(0x08, 0x32)),
                )

            assertEquals(50, restored.thresholdPercentage)
            assertEquals(LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT, restored.readoutText)
        }

    @Test
    fun `保存した空欄文言は読み出しても既定値に戻らない`() =
        runTest {
            val original = LmuWindowsRemainingVirtualEnergyPreferences(readoutText = "")
            val output = ByteArrayOutputStream()
            LmuWindowsRemainingVirtualEnergyPreferencesSerializer.writeTo(original, output)

            assertEquals(
                original,
                LmuWindowsRemainingVirtualEnergyPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray())),
            )
        }
}
