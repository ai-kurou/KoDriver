package kurou.kodriver

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.repository.AceWindowsBestLapTimeRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsGapRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsStatusRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachRepository
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsServerUseCasesTest {
    private val aceWindowsFuelRepository: AceWindowsFuelRepository = mockk()
    private val aceWindowsFlagRepository: AceWindowsFlagRepository = mockk()
    private val aceWindowsStatusRepository: AceWindowsStatusRepository = mockk()
    private val aceWindowsTyreCarcassTemperatureRepository: AceWindowsTyreCarcassTemperatureRepository = mockk()
    private val aceWindowsVehicleApproachRepository: AceWindowsVehicleApproachRepository = mockk()
    private val aceWindowsBestLapTimeRepository: AceWindowsBestLapTimeRepository = mockk()
    private val aceWindowsRemainingFuelLapsRepository: AceWindowsRemainingFuelLapsRepository = mockk()
    private val aceWindowsGapRepository: AceWindowsGapRepository = mockk()

    @Test
    fun `KoinのRepositoryから全UseCaseを組み立てる`() {
        every { aceWindowsFuelRepository.fuelStream() } returns emptyFlow()
        every { aceWindowsFlagRepository.flagStream() } returns emptyFlow()
        every { aceWindowsStatusRepository.statusStream() } returns emptyFlow()
        every { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns emptyFlow()
        every { aceWindowsVehicleApproachRepository.vehicleApproachStream() } returns emptyFlow()
        every { aceWindowsBestLapTimeRepository.bestLapTimeStream() } returns emptyFlow()
        every { aceWindowsRemainingFuelLapsRepository.remainingFuelLapsStream() } returns emptyFlow()
        every { aceWindowsGapRepository.gapStream() } returns emptyFlow()
        val koin =
            startKoin {
                modules(
                    module {
                        single<AceWindowsFuelRepository> { aceWindowsFuelRepository }
                        single<AceWindowsFlagRepository> { aceWindowsFlagRepository }
                        single<AceWindowsStatusRepository> { aceWindowsStatusRepository }
                        single<AceWindowsTyreCarcassTemperatureRepository> {
                            aceWindowsTyreCarcassTemperatureRepository
                        }
                        single<AceWindowsVehicleApproachRepository> { aceWindowsVehicleApproachRepository }
                        single<AceWindowsBestLapTimeRepository> { aceWindowsBestLapTimeRepository }
                        single<AceWindowsRemainingFuelLapsRepository> { aceWindowsRemainingFuelLapsRepository }
                        single<AceWindowsGapRepository> { aceWindowsGapRepository }
                    },
                )
            }.koin
        try {
            val useCases = aceWindowsServerUseCasesFrom(koin)
            useCases.observeAceWindowsFuel()
            useCases.observeAceWindowsFlag()
            useCases.observeAceWindowsStatus()
            useCases.observeAceWindowsTyreCarcassTemperature()
            useCases.observeAceWindowsVehicleApproach()
            useCases.observeAceWindowsBestLapTime()
            useCases.observeAceWindowsRemainingFuelLaps()
            useCases.observeAceWindowsGap()
            verify(exactly = 1) { aceWindowsFuelRepository.fuelStream() }
            verify(exactly = 1) { aceWindowsFlagRepository.flagStream() }
            verify(exactly = 1) { aceWindowsStatusRepository.statusStream() }
            verify(exactly = 1) { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { aceWindowsVehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { aceWindowsBestLapTimeRepository.bestLapTimeStream() }
            verify(exactly = 1) { aceWindowsRemainingFuelLapsRepository.remainingFuelLapsStream() }
            verify(exactly = 1) { aceWindowsGapRepository.gapStream() }
            confirmVerified(
                aceWindowsFuelRepository,
                aceWindowsFlagRepository,
                aceWindowsStatusRepository,
                aceWindowsTyreCarcassTemperatureRepository,
                aceWindowsVehicleApproachRepository,
                aceWindowsBestLapTimeRepository,
                aceWindowsRemainingFuelLapsRepository,
                aceWindowsGapRepository,
            )
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `空Repositoryから組み立てた全UseCaseは値を配信しない`() =
        runTest {
            val useCases = emptyAceWindowsServerUseCases()
            assertEquals(emptyList(), useCases.observeAceWindowsFuel().toList())
            assertEquals(emptyList(), useCases.observeAceWindowsFlag().toList())
            assertEquals(emptyList(), useCases.observeAceWindowsStatus().toList())
            assertEquals(emptyList(), useCases.observeAceWindowsTyreCarcassTemperature().toList())
            assertEquals(emptyList(), useCases.observeAceWindowsVehicleApproach().toList())
            assertEquals(emptyList(), useCases.observeAceWindowsBestLapTime().toList())
            assertEquals(emptyList(), useCases.observeAceWindowsRemainingFuelLaps().toList())
            assertEquals(emptyList(), useCases.observeAceWindowsGap().toList())
        }
}
