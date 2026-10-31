package kurou.kodriver.core.lmuwindowsdata.preferences

import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_PIT_TIMING_FUEL_LAPS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

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
    fun `燃料のデフォルト値は両方とも3周`() {
        assertEquals(
            LmuWindowsPitTimingPreferences(
                virtualEnergyLaps = LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT,
                fuelLaps = LMU_WINDOWS_PIT_TIMING_FUEL_LAPS_DEFAULT,
            ),
            LmuWindowsPitTimingPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `燃料の書き込んだ値を読み出せる`() =
        runTest {
            val original =
                LmuWindowsPitTimingPreferences(
                    virtualEnergyLaps = 5,
                    fuelLaps = 1,
                    fuelReadoutText = "タイヤ残り{laps}周",
                    fuelImminentReadoutText = "タイヤ交換へ",
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

    @Test
    fun `燃料の保存した空欄文言は読み出しても既定値に戻らない`() =
        runTest {
            val original = LmuWindowsPitTimingPreferences(fuelReadoutText = "", fuelImminentReadoutText = "")
            val output = ByteArrayOutputStream()
            LmuWindowsPitTimingPreferencesSerializer.writeTo(original, output)

            assertEquals(
                original,
                LmuWindowsPitTimingPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray())),
            )
        }

    @Test
    fun `燃料フィールドのない旧データは既存周回数を維持して燃料の既定値を使う`() =
        runTest {
            val restored =
                LmuWindowsPitTimingPreferencesSerializer.readFrom(
                    ByteArrayInputStream(byteArrayOf(0x08, 0x05, 0x10, 0x01)),
                )
            assertEquals(LmuWindowsPitTimingPreferences(virtualEnergyLaps = 5, tyreWearLaps = 1), restored)
        }
}
