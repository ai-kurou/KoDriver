package kurou.kodriver.feature.gt7ps5debugstatedetail

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.Gt7Ps5VehicleClassData
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5VehicleClassUseCase
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5DebugStateSourceTest {
    private val observeTelemetry: ObserveGt7Ps5UseCase = mockk()
    private val observeVehicleClass: ObserveGt7Ps5VehicleClassUseCase = mockk()
    private val markCardsReceived: (Set<Gt7Ps5DebugStateCardKey>) -> Unit = mockk()

    @Test
    fun `Sourceは各UseCaseを一度購読し受信ごとに対応カードだけ通知する`() =
        runTest(UnconfinedTestDispatcher()) {
            val telemetry = MutableSharedFlow<Gt7Ps5TelemetryData>()
            val vehicleClass = MutableSharedFlow<Gt7Ps5VehicleClassData>()
            val telemetryKeys =
                setOf(
                    Gt7Ps5DebugStateCardKey.CURRENT_LAP,
                    Gt7Ps5DebugStateCardKey.BEST_LAP,
                    Gt7Ps5DebugStateCardKey.TYRE_TEMPERATURE,
                    Gt7Ps5DebugStateCardKey.FUEL_CONSUMPTION,
                )
            val vehicleClassKeys = setOf(Gt7Ps5DebugStateCardKey.VEHICLE_CLASS)
            every { observeTelemetry() } returns telemetry
            every { observeVehicleClass() } returns vehicleClass
            every { markCardsReceived(telemetryKeys) } returns Unit
            every { markCardsReceived(vehicleClassKeys) } returns Unit
            val source =
                Gt7Ps5DebugStateSource(
                    backgroundScope,
                    Gt7Ps5DebugStateUseCases(observeTelemetry, observeVehicleClass),
                    markCardsReceived,
                )
            testScheduler.runCurrent()
            assertEquals(Gt7Ps5DebugState(), source.state.first())
            assertEquals(1, telemetry.subscriptionCount.value)
            assertEquals(1, vehicleClass.subscriptionCount.value)

            val receivedTelemetry = sampleGt7Ps5Telemetry()
            val category = Gt7Ps5VehicleClassData("GR3")
            telemetry.emit(receivedTelemetry)
            vehicleClass.emit(category)
            assertEquals(
                Gt7Ps5DebugState(receivedTelemetry, category),
                source.state.first { it.telemetry == receivedTelemetry && it.vehicleClass == category },
            )
            telemetry.emit(receivedTelemetry)
            vehicleClass.emit(category)

            testScheduler.runCurrent()
            verify(exactly = 1) { observeTelemetry() }
            verify(exactly = 1) { observeVehicleClass() }
            verify(exactly = 2) { markCardsReceived(telemetryKeys) }
            verify(exactly = 2) { markCardsReceived(vehicleClassKeys) }
            confirmVerified(observeTelemetry, observeVehicleClass, markCardsReceived)
        }
}
