package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.lifecycle.viewModelScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsBrakeTemperatureRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsBrakeWearRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsCarGapRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsClassCarGapRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitStatusRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreDetachedRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVirtualEnergyRepository
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
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.DebugReadoutRequestRepository
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository
import kurou.kodriver.domain.repository.HotkeyRepository
import kurou.kodriver.domain.repository.ReadoutHotkeyPreferencesRepository
import kurou.kodriver.domain.usecase.RequestDebugReadoutUseCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsDebugStateDetailModuleTest {
    private val debugRequestRepository: DebugReadoutRequestRepository = mockk()

    private val debugStateCardOrderPreferencesRepository: DebugStateCardOrderPreferencesRepository = mockk()
    private val lmuWindowsBrakeTemperatureRepository: LmuWindowsBrakeTemperatureRepository = mockk()
    private val lmuWindowsBrakeWearRepository: LmuWindowsBrakeWearRepository = mockk()
    private val lmuWindowsFlagRepository: LmuWindowsFlagRepository = mockk()
    private val lmuWindowsPitStatusRepository: LmuWindowsPitStatusRepository = mockk()
    private val lmuWindowsRepository: LmuWindowsRepository = mockk()
    private val lmuWindowsTyreCarcassTemperatureRepository: LmuWindowsTyreCarcassTemperatureRepository = mockk()
    private val carGapRepository: LmuWindowsCarGapRepository = mockk()
    private val classCarGapRepository: LmuWindowsClassCarGapRepository = mockk()
    private val lmuWindowsTyreDetachedRepository: LmuWindowsTyreDetachedRepository = mockk()
    private val lmuWindowsVehicleApproachRepository: LmuWindowsVehicleApproachRepository = mockk()
    private val lmuWindowsVehicleClassRepository: LmuWindowsVehicleClassRepository = mockk()
    private val lmuWindowsVehicleDamageRepository: LmuWindowsVehicleDamageRepository = mockk()
    private val lmuWindowsVirtualEnergyRepository: LmuWindowsVirtualEnergyRepository = mockk()
    private val otherDebugStateCardOrderPreferencesRepository: DebugStateCardOrderPreferencesRepository = mockk()

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
        every { debugStateCardOrderPreferencesRepository.observeCardOrder() } returns emptyFlow()
        every { lmuWindowsBrakeTemperatureRepository.brakeTemperatureStream() } returns emptyFlow()
        every { lmuWindowsBrakeWearRepository.brakeWearStream() } returns emptyFlow()
        every { lmuWindowsFlagRepository.flagStream() } returns emptyFlow()
        every { lmuWindowsPitStatusRepository.pitStatusStream() } returns emptyFlow()
        every { lmuWindowsRepository.telemetryStream() } returns emptyFlow()
        every { lmuWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns emptyFlow()
        every { carGapRepository.carGapStream() } returns emptyFlow()
        every { classCarGapRepository.classCarGapStream() } returns emptyFlow()
        every { lmuWindowsTyreDetachedRepository.tyreDetachedStream() } returns emptyFlow()
        every { lmuWindowsVehicleApproachRepository.vehicleApproachStream() } returns emptyFlow()
        every { lmuWindowsVehicleClassRepository.vehicleClassStream() } returns emptyFlow()
        every { lmuWindowsVehicleDamageRepository.vehicleDamageStream() } returns emptyFlow()
        every { lmuWindowsVirtualEnergyRepository.virtualEnergyStream() } returns emptyFlow()
        val koin =
            koinApplication {
                modules(
                    lmuWindowsDebugStateDetailModule,
                    module {
                        single { FakeReadoutHotkeyRepository() }
                        single<HotkeyRepository> { get<FakeReadoutHotkeyRepository>() }
                        single<ReadoutHotkeyPreferencesRepository> { get<FakeReadoutHotkeyRepository>() }
                        single<DebugReadoutRequestRepository> { debugRequestRepository }
                        single<DebugStateCardOrderPreferencesRepository>(named(Simulator.LmuWindows.id)) {
                            debugStateCardOrderPreferencesRepository
                        }
                        single<LmuWindowsBrakeTemperatureRepository> { lmuWindowsBrakeTemperatureRepository }
                        single<LmuWindowsBrakeWearRepository> { lmuWindowsBrakeWearRepository }
                        single<LmuWindowsFlagRepository> { lmuWindowsFlagRepository }
                        single<LmuWindowsPitStatusRepository> { lmuWindowsPitStatusRepository }
                        single<LmuWindowsRepository> { lmuWindowsRepository }
                        single<LmuWindowsTyreCarcassTemperatureRepository> {
                            lmuWindowsTyreCarcassTemperatureRepository
                        }
                        single<LmuWindowsCarGapRepository> { carGapRepository }
                        single<LmuWindowsClassCarGapRepository> { classCarGapRepository }
                        single<LmuWindowsTyreDetachedRepository> { lmuWindowsTyreDetachedRepository }
                        single<LmuWindowsVehicleApproachRepository> { lmuWindowsVehicleApproachRepository }
                        single<LmuWindowsVehicleClassRepository> { lmuWindowsVehicleClassRepository }
                        single<LmuWindowsVehicleDamageRepository> { lmuWindowsVehicleDamageRepository }
                        single<LmuWindowsVirtualEnergyRepository> { lmuWindowsVirtualEnergyRepository }
                    },
                )
            }.koin
        try {
            assertNotNull(koin.get<LmuWindowsDebugStateUseCases>())
            assertNotNull(koin.get<RequestDebugReadoutUseCase>())
            assertNotNull(koin.get<DebugStateCardOrderUseCases>())
            assertNotNull(koin.get<ReadoutHotkeyUseCases>())
            assertNotNull(koin.get<ObserveLmuWindowsRaceFlagsUseCase>())
            assertNotNull(koin.get<ObserveLmuWindowsVirtualEnergyUseCase>())
            assertNotNull(koin.get<ObserveLmuWindowsUseCase>())
            assertNotNull(koin.get<ObserveLmuWindowsVehicleApproachUseCase>())
            assertNotNull(koin.get<ObserveLmuWindowsTyreCarcassTemperatureUseCase>())
            assertNotNull(koin.get<ObserveLmuWindowsBrakeTemperatureUseCase>())
            assertNotNull(koin.get<ObserveLmuWindowsBrakeWearRemainingUseCase>())
            assertNotNull(koin.get<ObserveLmuWindowsVehicleClassUseCase>())
            assertNotNull(koin.get<ObserveLmuWindowsPitStatusUseCase>())
            assertNotNull(koin.get<ObserveLmuWindowsVehicleDamageUseCase>())
            assertNotNull(koin.get<ObserveLmuWindowsCarGapUseCase>())
            assertNotNull(koin.get<ObserveLmuWindowsClassCarGapUseCase>())
            assertNotNull(koin.get<ObserveLmuWindowsTyreDetachedUseCase>())
            val viewModel = koin.get<LmuWindowsDebugStateDetailViewModel>()
            try {
                assertNotNull(viewModel)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        } finally {
            koin.close()
        }
    }

    @Test
    fun `カード順序の保存はLMU用Repositoryだけに届く`() =
        runTest {
            val order = listOf("SESSION", "SIMULATOR")
            coEvery { debugStateCardOrderPreferencesRepository.saveCardOrder(order) } returns Unit
            val koin =
                koinApplication {
                    modules(
                        lmuWindowsDebugStateDetailModule,
                        module {
                            single<DebugStateCardOrderPreferencesRepository>(named(Simulator.LmuWindows.id)) {
                                debugStateCardOrderPreferencesRepository
                            }
                            single<DebugStateCardOrderPreferencesRepository>(named(Simulator.AceWindows.id)) {
                                otherDebugStateCardOrderPreferencesRepository
                            }
                        },
                    )
                }.koin
            try {
                koin.get<DebugStateCardOrderUseCases>().saveCardOrder(order)
            } finally {
                koin.close()
            }

            coVerify(exactly = 1) { debugStateCardOrderPreferencesRepository.saveCardOrder(order) }
            coVerify(exactly = 0) { otherDebugStateCardOrderPreferencesRepository.saveCardOrder(order) }
            confirmVerified(debugStateCardOrderPreferencesRepository, otherDebugStateCardOrderPreferencesRepository)
        }
}
