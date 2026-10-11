package kurou.kodriver

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsBrakeTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsBrakeWearRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.repository.LmuWindowsPitStatusRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreDetachedRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreWearRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.repository.LmuWindowsVirtualEnergyRepository
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsServerUseCasesTest {
    private val lmuWindowsFlagRepository: LmuWindowsFlagRepository = mockk()
    private val lmuWindowsVehicleApproachRepository: LmuWindowsVehicleApproachRepository = mockk()
    private val lmuWindowsVehicleDamageRepository: LmuWindowsVehicleDamageRepository = mockk()
    private val lmuWindowsTyreCarcassTemperatureRepository: LmuWindowsTyreCarcassTemperatureRepository = mockk()
    private val lmuWindowsBrakeTemperatureRepository: LmuWindowsBrakeTemperatureRepository = mockk()
    private val lmuWindowsVehicleClassRepository: LmuWindowsVehicleClassRepository = mockk()
    private val lmuWindowsTyreWearRepository: LmuWindowsTyreWearRepository = mockk()
    private val lmuWindowsRepository: LmuWindowsRepository = mockk()
    private val lmuWindowsVirtualEnergyRepository: LmuWindowsVirtualEnergyRepository = mockk()
    private val lmuWindowsPitStatusRepository: LmuWindowsPitStatusRepository = mockk()
    private val lmuWindowsBrakeWearRepository: LmuWindowsBrakeWearRepository = mockk()
    private val lmuWindowsTyreDetachedRepository: LmuWindowsTyreDetachedRepository = mockk()

    @Test
    fun `KoinのRepositoryから全UseCaseを組み立てる`() {
        every { lmuWindowsFlagRepository.flagStream() } returns emptyFlow()
        every { lmuWindowsVehicleApproachRepository.vehicleApproachStream() } returns emptyFlow()
        every { lmuWindowsVehicleDamageRepository.vehicleDamageStream() } returns emptyFlow()
        every { lmuWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns emptyFlow()
        every { lmuWindowsBrakeTemperatureRepository.brakeTemperatureStream() } returns emptyFlow()
        every { lmuWindowsVehicleClassRepository.vehicleClassStream() } returns emptyFlow()
        every { lmuWindowsTyreWearRepository.tyreWearStream() } returns emptyFlow()
        every { lmuWindowsRepository.telemetryStream() } returns emptyFlow()
        every { lmuWindowsVirtualEnergyRepository.virtualEnergyStream() } returns emptyFlow()
        every { lmuWindowsPitStatusRepository.pitStatusStream() } returns emptyFlow()
        every { lmuWindowsBrakeWearRepository.brakeWearStream() } returns emptyFlow()
        every { lmuWindowsTyreDetachedRepository.tyreDetachedStream() } returns emptyFlow()
        val koin =
            startKoin {
                modules(
                    module {
                        single<LmuWindowsFlagRepository> { lmuWindowsFlagRepository }
                        single<LmuWindowsVehicleApproachRepository> { lmuWindowsVehicleApproachRepository }
                        single<LmuWindowsVehicleDamageRepository> { lmuWindowsVehicleDamageRepository }
                        single<LmuWindowsTyreCarcassTemperatureRepository> {
                            lmuWindowsTyreCarcassTemperatureRepository
                        }
                        single<LmuWindowsBrakeTemperatureRepository> { lmuWindowsBrakeTemperatureRepository }
                        single<LmuWindowsVehicleClassRepository> { lmuWindowsVehicleClassRepository }
                        single<LmuWindowsTyreWearRepository> { lmuWindowsTyreWearRepository }
                        single<LmuWindowsRepository> { lmuWindowsRepository }
                        single<LmuWindowsVirtualEnergyRepository> { lmuWindowsVirtualEnergyRepository }
                        single<LmuWindowsPitStatusRepository> { lmuWindowsPitStatusRepository }
                        single<LmuWindowsBrakeWearRepository> { lmuWindowsBrakeWearRepository }
                        single<LmuWindowsTyreDetachedRepository> { lmuWindowsTyreDetachedRepository }
                    },
                )
            }.koin
        try {
            val useCases = lmuWindowsServerUseCasesFrom(koin)
            useCases.observeLmuWindowsRaceFlags()
            useCases.observeLmuWindowsVehicleApproach()
            useCases.observeLmuWindowsVehicleDamage()
            useCases.observeLmuWindowsTyreCarcassTemperature()
            useCases.observeLmuWindowsBrakeTemperature()
            useCases.observeLmuWindowsVehicleClass()
            useCases.observeLmuWindowsTyreWear()
            useCases.observeLmuWindows()
            useCases.observeLmuWindowsVirtualEnergy()
            useCases.observeLmuWindowsPitStatus()
            useCases.observeLmuWindowsBrakeWear()
            useCases.observeLmuWindowsTyreDetached()
            verify(exactly = 1) { lmuWindowsFlagRepository.flagStream() }
            verify(exactly = 1) { lmuWindowsVehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { lmuWindowsVehicleDamageRepository.vehicleDamageStream() }
            verify(exactly = 1) { lmuWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { lmuWindowsBrakeTemperatureRepository.brakeTemperatureStream() }
            verify(exactly = 1) { lmuWindowsVehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { lmuWindowsTyreWearRepository.tyreWearStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 1) { lmuWindowsVirtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { lmuWindowsBrakeWearRepository.brakeWearStream() }
            verify(exactly = 1) { lmuWindowsTyreDetachedRepository.tyreDetachedStream() }
            confirmVerified(
                lmuWindowsFlagRepository,
                lmuWindowsVehicleApproachRepository,
                lmuWindowsVehicleDamageRepository,
                lmuWindowsTyreCarcassTemperatureRepository,
                lmuWindowsBrakeTemperatureRepository,
                lmuWindowsVehicleClassRepository,
                lmuWindowsTyreWearRepository,
                lmuWindowsRepository,
                lmuWindowsVirtualEnergyRepository,
                lmuWindowsPitStatusRepository,
                lmuWindowsBrakeWearRepository,
                lmuWindowsTyreDetachedRepository,
            )
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `空Repositoryから組み立てた全UseCaseは値を配信しない`() =
        runTest {
            val useCases = emptyLmuWindowsServerUseCases()
            assertEquals(emptyList(), useCases.observeLmuWindowsRaceFlags().toList())
            assertEquals(emptyList(), useCases.observeLmuWindowsVehicleApproach().toList())
            assertEquals(emptyList(), useCases.observeLmuWindowsVehicleDamage().toList())
            assertEquals(emptyList(), useCases.observeLmuWindowsTyreCarcassTemperature().toList())
            assertEquals(emptyList(), useCases.observeLmuWindowsBrakeTemperature().toList())
            assertEquals(emptyList(), useCases.observeLmuWindowsVehicleClass().toList())
            assertEquals(emptyList(), useCases.observeLmuWindowsTyreWear().toList())
            assertEquals(emptyList(), useCases.observeLmuWindows().toList())
            assertEquals(emptyList(), useCases.observeLmuWindowsVirtualEnergy().toList())
            assertEquals(emptyList(), useCases.observeLmuWindowsPitStatus().toList())
            assertEquals(emptyList(), useCases.observeLmuWindowsBrakeWear().toList())
            assertEquals(emptyList(), useCases.observeLmuWindowsTyreDetached().toList())
        }
}
