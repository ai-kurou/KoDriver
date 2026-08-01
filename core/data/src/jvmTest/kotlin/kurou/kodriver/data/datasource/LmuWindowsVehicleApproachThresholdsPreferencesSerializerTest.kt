package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.model.LmuWindowsVehicleApproachThresholdsPreferences
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LmuWindowsVehicleApproachThresholdsPreferencesSerializerTest {

    @Test
    fun `デフォルト値は縦方向5m・横方向5m・継続時間7秒`() {
        val expected =
            LmuWindowsVehicleApproachThresholdsPreferences(
            longitudinalThresholdMeters = LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT,
            lateralThresholdMeters = LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT,
            sustainedApproachDurationSeconds = LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT,
        )
        assertEquals(expected, LmuWindowsVehicleApproachThresholdsPreferencesSerializer.defaultValue)
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
        val original =
            LmuWindowsVehicleApproachThresholdsPreferences(
            longitudinalThresholdMeters = 25.0,
            lateralThresholdMeters = 4.5,
            sustainedApproachDurationSeconds = 8,
        )
        val output = ByteArrayOutputStream()
        LmuWindowsVehicleApproachThresholdsPreferencesSerializer.writeTo(original, output)

        val restored =
            LmuWindowsVehicleApproachThresholdsPreferencesSerializer.readFrom(
            ByteArrayInputStream(output.toByteArray()),
        )

        assertEquals(original, restored)
    }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
        val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

        assertFailsWith<CorruptionException> {
            LmuWindowsVehicleApproachThresholdsPreferencesSerializer.readFrom(corrupt)
        }
    }
}
