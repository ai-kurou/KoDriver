package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsEngineData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelUnit
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsInputsData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTimingData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearRatio
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyRatio
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.PressureKpa
import kurou.kodriver.domain.model.WheelIndex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LmuFuelConsumptionCalculatorTest {
    @Test
    fun `残量がゼロなら消費率は計算できて残り周回数はゼロ`() {
        val result =
            calculateLmuVirtualEnergyConsumption(
                virtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = LmuWindowsVirtualEnergyRatio(0.0)),
                telemetry = sampleLmuTelemetry(currentLap = 1),
                baseline = LmuWindowsVirtualEnergyBaseline(1.0, 0),
            )

        assertEquals(100.0, result?.consumptionPerLap)
        assertEquals(0, result?.remainingLaps)
        assertEquals(0.0, result?.preciseRemainingLaps)
        assertEquals(
            0.0,
            calculateLmuTyreWearRemainingLaps(
                sampleLmuTelemetry(currentLap = 1, wheels = mapOf(WheelIndex.FRONT_LEFT to 0.0)),
                baseline = LmuWindowsTyreWearBaseline(1.0, 0),
            ),
        )
    }

    @Test
    fun `タイヤ摩耗は最小の残溝が後のホイールにあっても採用する`() {
        assertEquals(
            5.0,
            calculateLmuTyreWearRemainingLaps(
                sampleLmuTelemetry(
                    currentLap = 5,
                    wheels = mapOf(WheelIndex.FRONT_LEFT to 0.8, WheelIndex.FRONT_RIGHT to 0.5),
                ),
                baseline = LmuWindowsTyreWearBaseline(1.0, 0),
            ),
        )
    }

    @Test
    fun `LMU 5周消費後の残量50パーセントなら1周あたり10パーセント消費で残り5周`() {
        val result =
            calculateLmuVirtualEnergyConsumption(
                virtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = LmuWindowsVirtualEnergyRatio(0.5)),
                telemetry = sampleLmuTelemetry(currentLap = 5),
                baseline = LmuWindowsVirtualEnergyBaseline(1.0, 0),
            )

        assertEquals(10.0, result?.consumptionPerLap)
        assertEquals(5, result?.remainingLaps)
        assertEquals(5.0, result?.preciseRemainingLaps)
    }

    @Test
    fun `LMU virtualEnergyがnullの場合はnullを返す`() {
        assertNull(
            calculateLmuVirtualEnergyConsumption(
                virtualEnergy = null,
                telemetry = sampleLmuTelemetry(currentLap = 5),
                baseline = LmuWindowsVirtualEnergyBaseline(1.0, 0),
            ),
        )
    }

    @Test
    fun `LMU telemetryがnullの場合はnullを返す`() {
        assertNull(
            calculateLmuVirtualEnergyConsumption(
                virtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = LmuWindowsVirtualEnergyRatio(0.5)),
                telemetry = null,
                baseline = LmuWindowsVirtualEnergyBaseline(1.0, 0),
            ),
        )
    }

    @Test
    fun `LMU currentLapが0以下の場合はnullを返す`() {
        assertNull(
            calculateLmuVirtualEnergyConsumption(
                virtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = LmuWindowsVirtualEnergyRatio(0.5)),
                telemetry = sampleLmuTelemetry(currentLap = 0),
                baseline = LmuWindowsVirtualEnergyBaseline(1.0, 0),
            ),
        )
    }

    @Test
    fun `LMU 消費量が0以下（残量が減っていない）場合はnullを返す`() {
        assertNull(
            calculateLmuVirtualEnergyConsumption(
                virtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = LmuWindowsVirtualEnergyRatio(1.0)),
                telemetry = sampleLmuTelemetry(currentLap = 3),
                baseline = LmuWindowsVirtualEnergyBaseline(1.0, 0),
            ),
        )
    }

    @Test
    fun `LMU バーチャルエネルギー残量を割合で返す`() {
        assertEquals(
            50.0,
            calculateLmuVirtualEnergyRemainingPercent(
                LmuWindowsVirtualEnergyData(remainingRatio = LmuWindowsVirtualEnergyRatio(0.5)),
            ),
        )
        assertNull(calculateLmuVirtualEnergyRemainingPercent(null))
    }

    @Test
    fun `タイヤ摩耗 5周で最も摩耗した輪の残溝が50パーセントなら1周あたり10パーセント摩耗で残り5周`() {
        val result =
            calculateLmuTyreWearRemainingLaps(
                sampleLmuTelemetry(
                    currentLap = 5,
                    wheels = mapOf(WheelIndex.FRONT_LEFT to 0.5, WheelIndex.FRONT_RIGHT to 0.8),
                ),
                baseline = LmuWindowsTyreWearBaseline(1.0, 0),
            )

        assertEquals(5.0, result)
    }

    @Test
    fun `タイヤ摩耗 telemetryがnullの場合はnullを返す`() {
        assertNull(calculateLmuTyreWearRemainingLaps(null, baseline = LmuWindowsTyreWearBaseline(1.0, 0)))
    }

    @Test
    fun `タイヤ摩耗 currentLapが0以下の場合はnullを返す`() {
        assertNull(
            calculateLmuTyreWearRemainingLaps(
                sampleLmuTelemetry(currentLap = 0, wheels = mapOf(WheelIndex.FRONT_LEFT to 0.5)),
                baseline = LmuWindowsTyreWearBaseline(1.0, 0),
            ),
        )
    }

    @Test
    fun `タイヤ摩耗 wheelsが空の場合はnullを返す`() {
        assertNull(
            calculateLmuTyreWearRemainingLaps(
                sampleLmuTelemetry(currentLap = 5, wheels = emptyMap()),
                baseline = LmuWindowsTyreWearBaseline(1.0, 0),
            ),
        )
    }

    @Test
    fun `タイヤ摩耗 摩耗量が0以下（残溝が減っていない）場合はnullを返す`() {
        assertNull(
            calculateLmuTyreWearRemainingLaps(
                sampleLmuTelemetry(currentLap = 3, wheels = mapOf(WheelIndex.FRONT_LEFT to 1.0)),
                baseline = LmuWindowsTyreWearBaseline(1.0, 0),
            ),
        )
    }

    @Test
    fun `燃料は観測開始からの差分と経過周回数から残り周回数を推定する`() {
        // 20Lから開始し2周で10L消費したので、残り10Lは約2周分
        assertEquals(2.0, calculateLmuFuelRemainingLaps(fuelTelemetry(10.0, 5), LmuWindowsFuelBaseline(20.0, 3)))
        assertEquals(0.0, calculateLmuFuelRemainingLaps(fuelTelemetry(0.0, 5), LmuWindowsFuelBaseline(20.0, 3)))
    }

    @Test
    fun `燃料は消費量が未確定の場合はnullを返す`() {
        val baseline = LmuWindowsFuelBaseline(20.0, 3)
        assertNull(calculateLmuFuelRemainingLaps(null, baseline))
        assertNull(calculateLmuFuelRemainingLaps(fuelTelemetry(10.0, 5), null))
        assertNull(calculateLmuFuelRemainingLaps(fuelTelemetry(10.0, 3), baseline))
        assertNull(calculateLmuFuelRemainingLaps(fuelTelemetry(20.0, 5), baseline))
        assertNull(calculateLmuFuelRemainingLaps(fuelTelemetry(25.0, 5), baseline))
        listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY).forEach { current ->
            assertNull(calculateLmuFuelRemainingLaps(fuelTelemetry(current, 5), baseline))
        }
    }

    @Test
    fun `50パーセントから2周で40パーセントになれば残り8周になる`() {
        val telemetry = sampleLmuTelemetry(12, mapOf(WheelIndex.FRONT_LEFT to 0.4))
        val energy = LmuWindowsVirtualEnergyData(LmuWindowsVirtualEnergyRatio(0.4))
        val result = calculateLmuVirtualEnergyConsumption(energy, telemetry, LmuWindowsVirtualEnergyBaseline(0.5, 10))
        assertEquals(8.0, result!!.preciseRemainingLaps, 0.000001)
        assertEquals(5.0, result.consumptionPerLap, 0.000001)
        assertEquals(
            8.0,
            calculateLmuTyreWearRemainingLaps(telemetry, LmuWindowsTyreWearBaseline(0.5, 10))!!,
            0.000001,
        )
    }

    @Test
    fun `基準点なしまたは基準点以降の消費が未確定なら計算しない`() {
        val telemetry = sampleLmuTelemetry(12, mapOf(WheelIndex.FRONT_LEFT to 0.4))
        val energy = LmuWindowsVirtualEnergyData(LmuWindowsVirtualEnergyRatio(0.4))
        assertNull(calculateLmuVirtualEnergyConsumption(energy, telemetry, null))
        assertNull(calculateLmuTyreWearRemainingLaps(telemetry, null))
        listOf(12, 13).forEach { lap ->
            assertNull(
                calculateLmuVirtualEnergyConsumption(energy, telemetry, LmuWindowsVirtualEnergyBaseline(0.5, lap)),
            )
            assertNull(calculateLmuTyreWearRemainingLaps(telemetry, LmuWindowsTyreWearBaseline(0.5, lap)))
        }
        listOf(0.4, 0.3).forEach { ratio ->
            assertNull(
                calculateLmuVirtualEnergyConsumption(energy, telemetry, LmuWindowsVirtualEnergyBaseline(ratio, 10)),
            )
            assertNull(calculateLmuTyreWearRemainingLaps(telemetry, LmuWindowsTyreWearBaseline(ratio, 10)))
        }
        listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { ratio ->
            assertNull(
                calculateLmuVirtualEnergyConsumption(
                    LmuWindowsVirtualEnergyData(LmuWindowsVirtualEnergyRatio(ratio)),
                    telemetry,
                    LmuWindowsVirtualEnergyBaseline(0.5, 10),
                ),
            )
            assertNull(
                calculateLmuTyreWearRemainingLaps(
                    sampleLmuTelemetry(12, mapOf(WheelIndex.FRONT_LEFT to ratio)),
                    LmuWindowsTyreWearBaseline(0.5, 10),
                ),
            )
        }
    }

    private fun fuelTelemetry(
        current: Double,
        lap: Int,
    ) = sampleLmuTelemetry(lap).copy(
        fuel = LmuWindowsFuelData(LmuWindowsFuelUnit(current), LmuWindowsFuelUnit(100.0)),
    )

    private fun sampleLmuTelemetry(
        currentLap: Int,
        wheels: Map<WheelIndex, Double> = emptyMap(),
    ) = LmuWindowsTelemetryData(
        timestampMs = 0L,
        engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
        inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
        tyres =
            LmuWindowsTyreData(
                wheels =
                    wheels.mapValues { (_, wear) ->
                        LmuWindowsTyreWheelData(
                            surfaceTemperature = CelsiusReading(0f),
                            carcassTemperature = CelsiusReading(0f),
                            brakeTemperature = CelsiusReading(0f),
                            pressureKpa = PressureKpa(0.0),
                            wear = LmuWindowsTyreWearRatio(wear),
                        )
                    },
            ),
        fuel = LmuWindowsFuelData(currentLiters = LmuWindowsFuelUnit(0.0), capacityLiters = LmuWindowsFuelUnit(0.0)),
        timing =
            LmuWindowsTimingData(
                currentLapTimeMs = 0L,
                lastLapTimeMs = 0L,
                bestLapTimeMs = 0L,
                sector1Ms = 0L,
                sector1And2Ms = 0L,
                currentLap = currentLap,
                maxLaps = 0,
            ),
        vehicle =
            LmuWindowsVehicleData(
                localVelocityX = 0.0,
                localVelocityY = 0.0,
                localVelocityZ = 0.0,
                positionX = 0.0,
                positionY = 0.0,
                positionZ = 0.0,
            ),
    )
}
