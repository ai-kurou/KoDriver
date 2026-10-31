package kurou.kodriver.core.gt7ps5data.preferences

import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

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
    fun `空欄文言は読み出しても既定値に戻らない`() =
        runTest {
            val original = Gt7Ps5RemainingFuelPreferences(readoutText = "")
            val output = ByteArrayOutputStream()
            Gt7Ps5RemainingFuelPreferencesSerializer.writeTo(original, output)

            assertEquals(
                original,
                Gt7Ps5RemainingFuelPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray())),
            )
        }
}
