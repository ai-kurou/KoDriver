package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.model.Gt7Ps5RemainingFuelPreferences
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Gt7Ps5RemainingFuelPreferencesSerializerTest {
    @Test
    fun `デフォルト値は30パーセント`() {
        assertEquals(
            Gt7Ps5RemainingFuelPreferences(
                thresholdPercentage = GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT,
            ),
            Gt7Ps5RemainingFuelPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original = Gt7Ps5RemainingFuelPreferences(thresholdPercentage = 45)
            val output = ByteArrayOutputStream()
            Gt7Ps5RemainingFuelPreferencesSerializer.writeTo(original, output)

            val restored =
                Gt7Ps5RemainingFuelPreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                )

            assertEquals(original, restored)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                Gt7Ps5RemainingFuelPreferencesSerializer.readFrom(corrupt)
            }
        }
}
