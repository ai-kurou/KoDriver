package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AceWindowsRemainingFuelLapsPreferencesSerializerTest {
    @Test
    fun `デフォルト値は3周`() {
        assertEquals(
            AceWindowsRemainingFuelLapsPreferences(thresholdLaps = ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT),
            AceWindowsRemainingFuelLapsPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original = AceWindowsRemainingFuelLapsPreferences(thresholdLaps = 5)
            val output = ByteArrayOutputStream()
            AceWindowsRemainingFuelLapsPreferencesSerializer.writeTo(original, output)

            val restored =
                AceWindowsRemainingFuelLapsPreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                )

            assertEquals(original, restored)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                AceWindowsRemainingFuelLapsPreferencesSerializer.readFrom(corrupt)
            }
        }
}
