package kurou.kodriver.core.lmuwindowsdata.preferences

import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsRemainingFuelPreferencesSerializerTest {
    @Test
    fun `デフォルト値は閾値30と既定文言`() {
        assertEquals(
            LmuWindowsRemainingFuelPreferences(
                thresholdPercentage = LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT,
                readoutText = LMU_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT,
            ),
            LmuWindowsRemainingFuelPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `保存した空欄文言は読み出しても既定値に戻らない`() =
        runTest {
            val original = LmuWindowsRemainingFuelPreferences(readoutText = "")
            val output = ByteArrayOutputStream()
            LmuWindowsRemainingFuelPreferencesSerializer.writeTo(original, output)

            assertEquals(
                original,
                LmuWindowsRemainingFuelPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray())),
            )
        }
}
