package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.model.VehicleApproachPreferences
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VehicleApproachPreferencesSerializerTest {

    @Test
    fun `デフォルト値は初期設定を返す`() {
        assertEquals(
            VehicleApproachPreferences(
                skipFirstLap = true,
                startReadoutEnabled = true,
                startReadoutType = "car_left_right",
            ),
            VehicleApproachPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() = runTest {
        val original = VehicleApproachPreferences(
            skipFirstLap = true,
            startReadoutEnabled = false,
            startReadoutType = "left_right_approach",
        )
        val output = ByteArrayOutputStream()
        VehicleApproachPreferencesSerializer.writeTo(original, output)

        val restored = VehicleApproachPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

        assertEquals(original, restored)
    }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() = runTest {
        val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

        assertFailsWith<CorruptionException> {
            VehicleApproachPreferencesSerializer.readFrom(corrupt)
        }
    }
}
