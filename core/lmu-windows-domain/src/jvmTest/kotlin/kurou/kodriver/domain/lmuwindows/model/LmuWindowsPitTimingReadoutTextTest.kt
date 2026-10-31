package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.PitTimingSource
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsPitTimingReadoutTextTest {
    @Test
    fun `バーチャルエネルギーの1以上の周回数は通常既定文言を置換する`() {
        listOf(1, 3).forEach { laps ->
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT.replace("{laps}", laps.toString()),
                defaultLmuWindowsPitTimingReadoutText(PitTimingSource.VirtualEnergy, laps),
            )
        }
    }

    @Test
    fun `バーチャルエネルギーの0以下の周回数は切迫既定文言を返す`() {
        listOf(0, -1).forEach { laps ->
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT,
                defaultLmuWindowsPitTimingReadoutText(PitTimingSource.VirtualEnergy, laps),
            )
        }
    }

    @Test
    fun `タイヤ摩耗の1以上の周回数は通常既定文言を置換する`() {
        listOf(1, 3).forEach { laps ->
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT.replace("{laps}", laps.toString()),
                defaultLmuWindowsPitTimingReadoutText(PitTimingSource.TyreWear, laps),
            )
        }
    }

    @Test
    fun `燃料の1以上の周回数は通常既定文言を置換する`() {
        listOf(1, 3).forEach { laps ->
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_FUEL_READOUT_TEXT_DEFAULT.replace("{laps}", laps.toString()),
                defaultLmuWindowsPitTimingReadoutText(PitTimingSource.Fuel, laps),
            )
        }
    }

    @Test
    fun `タイヤ摩耗の0以下の周回数は切迫既定文言を返す`() {
        listOf(0, -1).forEach { laps ->
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT,
                defaultLmuWindowsPitTimingReadoutText(PitTimingSource.TyreWear, laps),
            )
        }
    }

    @Test
    fun `燃料の0以下の周回数は切迫既定文言を返す`() {
        listOf(0, -1).forEach { laps ->
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_FUEL_IMMINENT_READOUT_TEXT_DEFAULT,
                defaultLmuWindowsPitTimingReadoutText(PitTimingSource.Fuel, laps),
            )
        }
    }
}
