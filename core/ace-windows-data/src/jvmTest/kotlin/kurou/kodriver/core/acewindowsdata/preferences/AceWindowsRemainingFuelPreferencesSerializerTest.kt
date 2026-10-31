package kurou.kodriver.core.acewindowsdata.preferences

import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsRemainingFuelPreferencesSerializerTest {
    @Test
    fun `デフォルト値は thresholdPercentage が 30`() {
        assertEquals(
            AceWindowsRemainingFuelPreferences(
                thresholdPercentage = 30,
                readoutText = ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT,
            ),
            AceWindowsRemainingFuelPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `空欄文言は読み出しても既定値に戻らない`() =
        runTest {
            val original = AceWindowsRemainingFuelPreferences(readoutText = "")
            val output = ByteArrayOutputStream()
            AceWindowsRemainingFuelPreferencesSerializer.writeTo(original, output)

            assertEquals(
                original,
                AceWindowsRemainingFuelPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray())),
            )
        }
}
