package kurou.kodriver.core.lmuwindowsdata.preferences

import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

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
    fun `保存した空欄文言は読み出しても既定値に戻らない`() =
        runTest {
            val original = LmuWindowsRemainingVirtualEnergyPreferences(readoutText = "")
            val output = ByteArrayOutputStream()
            LmuWindowsRemainingVirtualEnergyPreferencesSerializer.writeTo(original, output)

            assertEquals(
                original,
                LmuWindowsRemainingVirtualEnergyPreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                ),
            )
        }
}
