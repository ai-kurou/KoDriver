package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.lifecycle.viewModelScope
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.repository.AceWindowsBestLapTimeRepository
import kurou.kodriver.domain.repository.AceWindowsFlagRepository
import kurou.kodriver.domain.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelLapsRepository
import kurou.kodriver.domain.repository.AceWindowsStatusRepository
import kurou.kodriver.domain.repository.AceWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveAceWindowsBestLapTimeUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsStatusUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveDebugStateCardOrderUseCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsDebugStateDetailModuleTest {
    private val aceWindowsBestLapTimeRepository: AceWindowsBestLapTimeRepository = mockk()
    private val aceWindowsFlagRepository: AceWindowsFlagRepository = mockk()
    private val aceWindowsFuelRepository: AceWindowsFuelRepository = mockk()
    private val aceWindowsRemainingFuelLapsRepository: AceWindowsRemainingFuelLapsRepository = mockk()
    private val aceWindowsStatusRepository: AceWindowsStatusRepository = mockk()
    private val aceWindowsTyreCarcassTemperatureRepository: AceWindowsTyreCarcassTemperatureRepository = mockk()
    private val aceWindowsVehicleApproachRepository: AceWindowsVehicleApproachRepository = mockk()
    private val debugStateCardOrderPreferencesRepository: DebugStateCardOrderPreferencesRepository = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ViewModelとすべてのUseCaseをRepositoryから解決できる`() {
        every { aceWindowsBestLapTimeRepository.bestLapTimeStream() } returns emptyFlow()
        every { aceWindowsFlagRepository.flagStream() } returns emptyFlow()
        every { aceWindowsFuelRepository.fuelStream() } returns emptyFlow()
        every { aceWindowsRemainingFuelLapsRepository.remainingFuelLapsStream() } returns emptyFlow()
        every { aceWindowsStatusRepository.statusStream() } returns emptyFlow()
        every { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns emptyFlow()
        every { aceWindowsVehicleApproachRepository.vehicleApproachStream() } returns emptyFlow()
        every { debugStateCardOrderPreferencesRepository.observeCardOrder() } returns emptyFlow()
        val koin =
            koinApplication {
                modules(
                    aceWindowsDebugStateDetailModule,
                    module {
                        single<AceWindowsBestLapTimeRepository> { aceWindowsBestLapTimeRepository }
                        single<AceWindowsFlagRepository> { aceWindowsFlagRepository }
                        single<AceWindowsFuelRepository> { aceWindowsFuelRepository }
                        single<AceWindowsRemainingFuelLapsRepository> { aceWindowsRemainingFuelLapsRepository }
                        single<AceWindowsStatusRepository> { aceWindowsStatusRepository }
                        single<AceWindowsTyreCarcassTemperatureRepository> {
                            aceWindowsTyreCarcassTemperatureRepository
                        }
                        single<AceWindowsVehicleApproachRepository> { aceWindowsVehicleApproachRepository }
                        single<DebugStateCardOrderPreferencesRepository> { debugStateCardOrderPreferencesRepository }
                    },
                )
            }.koin
        try {
            assertNotNull(koin.get<DebugStateCardOrderUseCases>())
            assertNotNull(koin.get<ObserveDebugStateCardOrderUseCase>())
            assertNotNull(koin.get<ResolveDebugStateCardOrderUseCase>())
            assertNotNull(koin.get<SaveDebugStateCardOrderUseCase>())
            assertNotNull(koin.get<AceWindowsDebugStateUseCases>())
            assertNotNull(koin.get<ObserveAceWindowsFuelUseCase>())
            assertNotNull(koin.get<ObserveAceWindowsFlagUseCase>())
            assertNotNull(koin.get<ObserveAceWindowsStatusUseCase>())
            assertNotNull(koin.get<ObserveAceWindowsTyreCarcassTemperatureUseCase>())
            assertNotNull(koin.get<ObserveAceWindowsVehicleApproachUseCase>())
            assertNotNull(koin.get<ObserveAceWindowsBestLapTimeUseCase>())
            assertNotNull(koin.get<ObserveAceWindowsRemainingFuelLapsUseCase>())
            val viewModel = koin.get<AceWindowsDebugStateDetailViewModel>()
            try {
                assertNotNull(viewModel)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        } finally {
            koin.close()
        }
    }
}
