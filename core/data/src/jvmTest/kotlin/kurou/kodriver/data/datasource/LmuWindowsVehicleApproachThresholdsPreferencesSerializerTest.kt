package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.model.LmuWindowsVehicleApproachThresholdsPreferences
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LmuWindowsVehicleApproachThresholdsPreferencesSerializerTest {

    @Test
    fun `デフォルト値は縦方向5m・横方向5m`() {
        val expected = LmuWindowsVehicleApproachThresholdsPreferences(
            longitudinalThresholdMeters = 5.0,
            lateralThresholdMeters = 5.0,
        )
        assertEquals(expected, LmuWindowsVehicleApproachThresholdsPreferencesSerializer.defaultValue)
    }

    @Test
    fun `書き込んだ値を読み出せる`() = runTest {
        val original = LmuWindowsVehicleApproachThresholdsPreferences(
            longitudinalThresholdMeters = 25.0,
            lateralThresholdMeters = 4.5,
        )
        val output = ByteArrayOutputStream()
        LmuWindowsVehicleApproachThresholdsPreferencesSerializer.writeTo(original, output)

        val restored = LmuWindowsVehicleApproachThresholdsPreferencesSerializer.readFrom(
            ByteArrayInputStream(output.toByteArray()),
        )

        assertEquals(original, restored)
    }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() = runTest {
        val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

        assertFailsWith<CorruptionException> {
            LmuWindowsVehicleApproachThresholdsPreferencesSerializer.readFrom(corrupt)
        }
    }
}
