package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LmuWindowsPitTimingPreferencesSerializerTest {
    @Test
    fun `デフォルト値は両方とも3周`() {
        assertEquals(
            LmuWindowsPitTimingPreferences(
                virtualEnergyLaps = LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT,
                tyreWearLaps = LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT,
            ),
            LmuWindowsPitTimingPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original =
                LmuWindowsPitTimingPreferences(
                    virtualEnergyLaps = 5,
                    tyreWearLaps = 1,
                    tyreWearReadoutText = "タイヤ残り{laps}周",
                    tyreWearImminentReadoutText = "タイヤ交換へ",
                    virtualEnergyReadoutText = "残り{laps}周",
                    virtualEnergyImminentReadoutText = "今すぐピットイン",
                )
            val output = ByteArrayOutputStream()
            LmuWindowsPitTimingPreferencesSerializer.writeTo(original, output)

            val restored =
                LmuWindowsPitTimingPreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                )

            assertEquals(original, restored)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                LmuWindowsPitTimingPreferencesSerializer.readFrom(corrupt)
            }
        }

    @Test
    fun `新しい文言フィールドがない旧データは既定文言を使用する`() =
        runTest {
            val restored =
                LmuWindowsPitTimingPreferencesSerializer.readFrom(
                    ByteArrayInputStream(byteArrayOf(0x08, 0x05, 0x10, 0x01)),
                )

            assertEquals(LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT, restored.tyreWearReadoutText)
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT,
                restored.tyreWearImminentReadoutText,
            )
            assertEquals(5, restored.virtualEnergyLaps)
            assertEquals(1, restored.tyreWearLaps)
            assertEquals(LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT, restored.virtualEnergyReadoutText)
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT,
                restored.virtualEnergyImminentReadoutText,
            )
        }

    @Test
    fun `VE文言を持つ旧データでもタイヤ摩耗の既定文言を使用する`() =
        runTest {
            val restored =
                LmuWindowsPitTimingPreferencesSerializer.readFrom(
                    ByteArrayInputStream(byteArrayOf(0x22, 0x02, 0x56, 0x45, 0x2A, 0x00)),
                )

            assertEquals("VE", restored.virtualEnergyReadoutText)
            assertEquals("", restored.virtualEnergyImminentReadoutText)
            assertEquals(LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT, restored.tyreWearReadoutText)
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT,
                restored.tyreWearImminentReadoutText,
            )
        }

    @Test
    fun `保存した空欄文言は読み出しても既定値に戻らない`() =
        runTest {
            val original = LmuWindowsPitTimingPreferences(tyreWearReadoutText = "", tyreWearImminentReadoutText = "")
            val output = ByteArrayOutputStream()
            LmuWindowsPitTimingPreferencesSerializer.writeTo(original, output)

            assertEquals(
                original,
                LmuWindowsPitTimingPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray())),
            )
        }
}
