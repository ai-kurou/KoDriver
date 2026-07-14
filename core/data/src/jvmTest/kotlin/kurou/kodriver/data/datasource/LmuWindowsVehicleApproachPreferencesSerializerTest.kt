package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.model.LmuWindowsVehicleApproachPreferences
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LmuWindowsVehicleApproachPreferencesSerializerTest {

    @Test
    fun `デフォルト値は初期設定を返す`() {
        assertEquals(
            LmuWindowsVehicleApproachPreferences(
                skipFirstLap = true,
                startReadoutType = "car_left_right",
            ),
            LmuWindowsVehicleApproachPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() = runTest {
        val original = LmuWindowsVehicleApproachPreferences(
            skipFirstLap = true,
            startReadoutType = "left_right_approach",
            enabledStates = mapOf("lmu_windows_vehicle_approach_sustained" to false),
            sustainedReadoutType = "left_right_sustained",
        )
        val output = ByteArrayOutputStream()
        LmuWindowsVehicleApproachPreferencesSerializer.writeTo(original, output)

        val restored = LmuWindowsVehicleApproachPreferencesSerializer.readFrom(
            ByteArrayInputStream(output.toByteArray()),
        )

        assertEquals(original, restored)
    }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() = runTest {
        val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

        assertFailsWith<CorruptionException> {
            LmuWindowsVehicleApproachPreferencesSerializer.readFrom(corrupt)
        }
    }
}
