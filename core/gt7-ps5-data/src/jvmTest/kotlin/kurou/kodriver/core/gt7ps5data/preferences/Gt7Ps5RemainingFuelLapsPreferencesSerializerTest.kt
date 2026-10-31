package kurou.kodriver.core.gt7ps5data.preferences

import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5RemainingFuelLapsPreferencesSerializerTest {
    @Test
    fun `デフォルト値は3周と既定文言`() {
        assertEquals(
            Gt7Ps5RemainingFuelLapsPreferences(
                remainingFuelLaps = GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT,
                readoutText = GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT,
                emptyReadoutText = GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT,
            ),
            Gt7Ps5RemainingFuelLapsPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `空欄文言は読み出しても既定値に戻らない`() =
        runTest {
            val original = Gt7Ps5RemainingFuelLapsPreferences(readoutText = "", emptyReadoutText = "")
            val output = ByteArrayOutputStream()
            Gt7Ps5RemainingFuelLapsPreferencesSerializer.writeTo(original, output)

            assertEquals(
                original,
                Gt7Ps5RemainingFuelLapsPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray())),
            )
        }
}
