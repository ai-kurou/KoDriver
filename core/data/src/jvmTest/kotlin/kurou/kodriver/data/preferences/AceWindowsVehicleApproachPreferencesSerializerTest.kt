package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AceWindowsVehicleApproachPreferencesSerializerTest {
    @Test
    fun `デフォルト値は各Defaults定数と一致する`() {
        assertEquals(
            AceWindowsVehicleApproachPreferences(
                thresholdMeters = ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT,
            ),
            AceWindowsVehicleApproachPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original =
                AceWindowsVehicleApproachPreferences(
                    thresholdMeters = 7.0,
                    enabledStates = mapOf("ace_windows_vehicle_approach_start_readout" to false),
                )
            val output = ByteArrayOutputStream()
            AceWindowsVehicleApproachPreferencesSerializer.writeTo(original, output)

            val restored =
                AceWindowsVehicleApproachPreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                )

            assertEquals(original, restored)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                AceWindowsVehicleApproachPreferencesSerializer.readFrom(corrupt)
            }
        }
}
