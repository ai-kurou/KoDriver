package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.Gt7Ps5FuelUnit
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FuelConsumptionCalculatorTest {
    @Test
    fun `GT7 3周で30L消費し残り40Lなら1周あたり10Lで残り4周`() {
        val result =
            calculateGt7FuelConsumption(
                sampleGt7Telemetry(lapCount = 3, gasLevel = 40f, gasCapacity = 70f),
            )

        assertEquals(10.0, result?.consumptionPerLap)
        assertEquals(4, result?.remainingLaps)
        assertEquals(4.0, result?.preciseRemainingLaps)
    }

    @Test
    fun `GT7 燃料残量を割合で返す`() {
        assertEquals(
            25.0,
            calculateGt7FuelRemainingPercent(sampleGt7Telemetry(lapCount = 0, gasLevel = 17.5f, gasCapacity = 70f)),
        )
    }

    @Test
    fun `GT7 燃料容量が0以下の場合は残量割合を返さない`() {
        assertNull(
            calculateGt7FuelRemainingPercent(
                sampleGt7Telemetry(lapCount = 0, gasLevel = 17.5f, gasCapacity = 0f),
            ),
        )
    }

    @Test
    fun `GT7 telemetryがnullの場合は残量割合を返さない`() {
        assertNull(calculateGt7FuelRemainingPercent(null))
    }

    @Test
    fun `GT7 telemetryがnullの場合はnullを返す`() {
        assertNull(calculateGt7FuelConsumption(null))
    }

    @Test
    fun `GT7 lapCountが0以下の場合はnullを返す`() {
        assertNull(calculateGt7FuelConsumption(sampleGt7Telemetry(lapCount = 0, gasLevel = 40f, gasCapacity = 70f)))
    }

    @Test
    fun `GT7 消費量が0以下（燃料が減っていない）場合はnullを返す`() {
        assertNull(calculateGt7FuelConsumption(sampleGt7Telemetry(lapCount = 3, gasLevel = 70f, gasCapacity = 70f)))
    }

    private fun sampleGt7Telemetry(
        lapCount: Int,
        gasLevel: Float,
        gasCapacity: Float,
    ) = Gt7Ps5TelemetryData(
        lapCount = lapCount,
        lapsInRace = 0,
        bestLapTimeMs = 0,
        gasLevel = Gt7Ps5FuelUnit(gasLevel),
        gasCapacity = Gt7Ps5FuelUnit(gasCapacity),
    )
}
