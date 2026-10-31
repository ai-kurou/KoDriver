package kurou.kodriver.core.lmuwindowsdata.preferences

import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

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
