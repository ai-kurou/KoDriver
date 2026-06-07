package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.model.ProximityThresholdsPreferences
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProximityThresholdsSerializerTest {

    @Test
    fun `デフォルト値は縦方向1m・横方向2m`() {
        val expected = ProximityThresholdsPreferences(
            longitudinalThresholdMeters = 1.0,
            lateralThresholdMeters = 2.0,
        )
        assertEquals(expected, ProximityThresholdsSerializer.defaultValue)
    }

    @Test
    fun `書き込んだ値を読み出せる`() = runTest {
        val original = ProximityThresholdsPreferences(longitudinalThresholdMeters = 25.0, lateralThresholdMeters = 4.5)
        val output = ByteArrayOutputStream()
        ProximityThresholdsSerializer.writeTo(original, output)

        val restored = ProximityThresholdsSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

        assertEquals(original, restored)
    }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() = runTest {
        val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

        assertFailsWith<CorruptionException> {
            ProximityThresholdsSerializer.readFrom(corrupt)
        }
    }
}
