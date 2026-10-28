package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsCarGapData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeTemperatureUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeWearRemainingUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsCarGapUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsClassCarGapUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitStatusUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreDetachedUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsDebugStateSourceTest {
    private val observeRaceFlags: ObserveLmuWindowsRaceFlagsUseCase = mockk()
    private val observeVirtualEnergy: ObserveLmuWindowsVirtualEnergyUseCase = mockk()
    private val observeTelemetry: ObserveLmuWindowsUseCase = mockk()
    private val observeVehicleApproach: ObserveLmuWindowsVehicleApproachUseCase = mockk()
    private val observeTyreCarcassTemperature: ObserveLmuWindowsTyreCarcassTemperatureUseCase = mockk()
    private val observeBrakeWear: ObserveLmuWindowsBrakeWearRemainingUseCase = mockk()
    private val observeBrakeTemperature: ObserveLmuWindowsBrakeTemperatureUseCase = mockk()
    private val observeVehicleClass: ObserveLmuWindowsVehicleClassUseCase = mockk()
    private val observePitStatus: ObserveLmuWindowsPitStatusUseCase = mockk()
    private val observeVehicleDamage: ObserveLmuWindowsVehicleDamageUseCase = mockk()
    private val observeTyreDetached: ObserveLmuWindowsTyreDetachedUseCase = mockk()
    private val observeCarGap: ObserveLmuWindowsCarGapUseCase = mockk()
    private val observeClassCarGap: ObserveLmuWindowsClassCarGapUseCase = mockk()

    @Test
    fun `時間差は未受信から購読し受信済み通知と更新を反映する`() =
        runTest {
            val gaps = MutableSharedFlow<LmuWindowsCarGapData>(replay = 1)
            every { observeRaceFlags() } returns emptyFlow()
            every { observeVirtualEnergy() } returns emptyFlow()
            every { observeTelemetry() } returns emptyFlow()
            every { observeVehicleApproach() } returns emptyFlow()
            every { observeTyreCarcassTemperature() } returns emptyFlow()
            every { observeBrakeWear() } returns emptyFlow()
            every { observeBrakeTemperature() } returns emptyFlow()
            every { observeVehicleClass() } returns emptyFlow()
            every { observePitStatus() } returns emptyFlow()
            every { observeVehicleDamage() } returns emptyFlow()
            every { observeTyreDetached() } returns emptyFlow()
            every { observeCarGap() } returns gaps
            every { observeClassCarGap() } returns emptyFlow()
            val received = mutableListOf<Set<LmuWindowsDebugStateCardKey>>()
            val source =
                LmuWindowsDebugStateSource(
                    scope = backgroundScope,
                    useCases =
                        LmuWindowsDebugStateUseCases(
                            observeRaceFlags = observeRaceFlags,
                            observeVirtualEnergy = observeVirtualEnergy,
                            observeTelemetry = observeTelemetry,
                            observeVehicleApproach = observeVehicleApproach,
                            observeTyreCarcassTemperature = observeTyreCarcassTemperature,
                            observeBrakeWear = observeBrakeWear,
                            observeBrakeTemperature = observeBrakeTemperature,
                            observeVehicleClass = observeVehicleClass,
                            observePitStatus = observePitStatus,
                            observeVehicleDamage = observeVehicleDamage,
                            observeTyreDetached = observeTyreDetached,
                            observeCarGap = observeCarGap,
                            observeClassCarGap = observeClassCarGap,
                        ),
                    markCardsReceived = { received.add(it) },
                    currentTimeMs = { 0L },
                    sideBySideTickIntervalMs = 100L,
                )
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { source.state.collect() }
            testScheduler.runCurrent()
            assertEquals(null, source.state.value.carGap)
            assertEquals(emptyList(), received)

            val first = LmuWindowsCarGapData(1.0, -2.0, 0.0, 3.0)
            gaps.emit(first)
            testScheduler.runCurrent()
            assertEquals(first, source.state.value.carGap)

            val updated = LmuWindowsCarGapData(-4.0, 5.0, 6.0, 0.0)
            gaps.emit(updated)
            testScheduler.runCurrent()
            assertEquals(updated, source.state.value.carGap)
            assertEquals(List(2) { setOf(LmuWindowsDebugStateCardKey.CAR_GAP) }, received)
            verify(exactly = 1) { observeRaceFlags() }
            verify(exactly = 1) { observeVirtualEnergy() }
            verify(exactly = 1) { observeTelemetry() }
            verify(exactly = 1) { observeVehicleApproach() }
            verify(exactly = 1) { observeTyreCarcassTemperature() }
            verify(exactly = 1) { observeBrakeWear() }
            verify(exactly = 1) { observeBrakeTemperature() }
            verify(exactly = 1) { observeVehicleClass() }
            verify(exactly = 1) { observePitStatus() }
            verify(exactly = 1) { observeVehicleDamage() }
            verify(exactly = 1) { observeTyreDetached() }
            verify(exactly = 1) { observeCarGap() }
            verify(exactly = 1) { observeClassCarGap() }
            confirmVerified(
                observeRaceFlags,
                observeVirtualEnergy,
                observeTelemetry,
                observeVehicleApproach,
                observeTyreCarcassTemperature,
                observeBrakeWear,
                observeBrakeTemperature,
                observeVehicleClass,
                observePitStatus,
                observeVehicleDamage,
                observeTyreDetached,
                observeCarGap,
                observeClassCarGap,
            )
        }

    @Test
    fun `同クラス時間差は未受信から購読し受信済み通知と更新を反映する`() =
        runTest {
            val gaps = MutableSharedFlow<LmuWindowsClassCarGapData>(replay = 1)
            every { observeRaceFlags() } returns emptyFlow()
            every { observeVirtualEnergy() } returns emptyFlow()
            every { observeTelemetry() } returns emptyFlow()
            every { observeVehicleApproach() } returns emptyFlow()
            every { observeTyreCarcassTemperature() } returns emptyFlow()
            every { observeBrakeWear() } returns emptyFlow()
            every { observeBrakeTemperature() } returns emptyFlow()
            every { observeVehicleClass() } returns emptyFlow()
            every { observePitStatus() } returns emptyFlow()
            every { observeVehicleDamage() } returns emptyFlow()
            every { observeTyreDetached() } returns emptyFlow()
            every { observeClassCarGap() } returns gaps
            every { observeCarGap() } returns emptyFlow()
            val received = mutableListOf<Set<LmuWindowsDebugStateCardKey>>()
            val source =
                LmuWindowsDebugStateSource(
                    scope = backgroundScope,
                    useCases =
                        LmuWindowsDebugStateUseCases(
                            observeRaceFlags = observeRaceFlags,
                            observeVirtualEnergy = observeVirtualEnergy,
                            observeTelemetry = observeTelemetry,
                            observeVehicleApproach = observeVehicleApproach,
                            observeTyreCarcassTemperature = observeTyreCarcassTemperature,
                            observeBrakeWear = observeBrakeWear,
                            observeBrakeTemperature = observeBrakeTemperature,
                            observeVehicleClass = observeVehicleClass,
                            observePitStatus = observePitStatus,
                            observeVehicleDamage = observeVehicleDamage,
                            observeTyreDetached = observeTyreDetached,
                            observeClassCarGap = observeClassCarGap,
                            observeCarGap = observeCarGap,
                        ),
                    markCardsReceived = { received.add(it) },
                    currentTimeMs = { 0L },
                    sideBySideTickIntervalMs = 100L,
                )
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { source.state.collect() }
            testScheduler.runCurrent()
            assertEquals(null, source.state.value.classCarGap)
            assertEquals(emptyList(), received)

            val first = LmuWindowsClassCarGapData(1.0, -2.0)
            gaps.emit(first)
            testScheduler.runCurrent()
            assertEquals(first, source.state.value.classCarGap)

            val updated = LmuWindowsClassCarGapData(null, null)
            gaps.emit(updated)
            testScheduler.runCurrent()
            assertEquals(updated, source.state.value.classCarGap)
            assertEquals(List(2) { setOf(LmuWindowsDebugStateCardKey.CLASS_CAR_GAP) }, received)
            verify(exactly = 1) { observeRaceFlags() }
            verify(exactly = 1) { observeVirtualEnergy() }
            verify(exactly = 1) { observeTelemetry() }
            verify(exactly = 1) { observeVehicleApproach() }
            verify(exactly = 1) { observeTyreCarcassTemperature() }
            verify(exactly = 1) { observeBrakeWear() }
            verify(exactly = 1) { observeBrakeTemperature() }
            verify(exactly = 1) { observeVehicleClass() }
            verify(exactly = 1) { observePitStatus() }
            verify(exactly = 1) { observeVehicleDamage() }
            verify(exactly = 1) { observeTyreDetached() }
            verify(exactly = 1) { observeClassCarGap() }
            verify(exactly = 1) { observeCarGap() }
            confirmVerified(
                observeRaceFlags,
                observeVirtualEnergy,
                observeTelemetry,
                observeVehicleApproach,
                observeTyreCarcassTemperature,
                observeBrakeWear,
                observeBrakeTemperature,
                observeVehicleClass,
                observePitStatus,
                observeVehicleDamage,
                observeTyreDetached,
                observeClassCarGap,
                observeCarGap,
            )
        }
}
