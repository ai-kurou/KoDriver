package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original =
                Gt7Ps5RemainingFuelLapsPreferences(
                    remainingFuelLaps = 5,
                    readoutText = "残り{laps}周",
                    emptyReadoutText = "燃料なし",
                )
            val output = ByteArrayOutputStream()
            Gt7Ps5RemainingFuelLapsPreferencesSerializer.writeTo(original, output)

            val restored =
                Gt7Ps5RemainingFuelLapsPreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                )

            assertEquals(original, restored)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                Gt7Ps5RemainingFuelLapsPreferencesSerializer.readFrom(corrupt)
            }
        }

    @Test
    fun `文言がない旧データは既定文言を使用する`() =
        runTest {
            val restored =
                Gt7Ps5RemainingFuelLapsPreferencesSerializer.readFrom(ByteArrayInputStream(byteArrayOf(0x08, 0x05)))

            assertEquals(5, restored.remainingFuelLaps)
            assertEquals(GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT, restored.readoutText)
            assertEquals(GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT, restored.emptyReadoutText)
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
