package kurou.kodriver.feature.lmuwindowsnarrator

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRepository
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsFuelReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.ManualReadoutHotkeyRepository
import kurou.kodriver.domain.repository.ManualReadoutRequestRepository
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveReadoutHotkeyUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class LmuWindowsNarratorModuleTest {
    private val setRepository: ManualReadoutSetPreferencesRepository = mockk()
    private val manualHotkeyRepository: ManualReadoutHotkeyRepository = mockk()
    private val requestRepository: ManualReadoutRequestRepository = mockk()
    private val lmuWindowsRepository: LmuWindowsRepository = mockk()
    private val observeEnergy: ObserveLmuWindowsVirtualEnergyUseCase = mockk()
    private val observeHotkey: ObserveReadoutHotkeyUseCase = mockk()
    private val observeThreshold: ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase = mockk()
    private val observeFuelThreshold: ObserveLmuWindowsRemainingFuelThresholdPercentageUseCase = mockk()
    private val observeFuelText: ObserveLmuWindowsRemainingFuelReadoutTextUseCase = mockk()

    @Test
    fun `仮想エネルギーのUseCase群は手動依頼とLMUのセットRepositoryも解決する`() =
        runTest {
            val request = ManualReadoutRequest(Simulator.LmuWindows.id, "set-1")
            val sets = listOf(ManualReadoutSet("set-1"))
            every { manualHotkeyRepository.pressStream() } returns flowOf(request)
            every { requestRepository.observeRequests() } returns flowOf(request)
            every { setRepository.observeSets() } returns flowOf(sets)
            val application =
                koinApplication {
                    modules(
                        lmuWindowsNarratorModule,
                        module {
                            single<ManualReadoutHotkeyRepository> { manualHotkeyRepository }
                            single<ManualReadoutRequestRepository> { requestRepository }
                            single<ManualReadoutSetPreferencesRepository>(named(Simulator.LmuWindows.id)) {
                                setRepository
                            }
                            single<LmuWindowsRepository> { lmuWindowsRepository }
                            factory { observeEnergy }
                            factory { observeHotkey }
                            factory { observeThreshold }
                            factory { observeFuelThreshold }
                            factory(named(Simulator.LmuWindows.id)) { observeFuelText }
                        },
                    )
                }
            try {
                val useCases = application.koin.get<RemainingVirtualEnergyUseCases>()
                val fuelUseCases = application.koin.get<RemainingFuelUseCases>()
                assertSame(observeEnergy, useCases.observeRemainingVirtualEnergy)
                assertSame(observeHotkey, useCases.observeReadoutHotkey)
                assertSame(observeThreshold, useCases.observeThresholdPercentage)
                assertSame(observeFuelThreshold, fuelUseCases.observeThresholdPercentage)
                assertSame(
                    observeFuelText,
                    application.koin.get<ObserveLmuWindowsRemainingFuelReadoutTextUseCase>(
                        named(Simulator.LmuWindows.id),
                    ),
                )
                application.koin.get<DetermineLmuWindowsFuelReadoutUseCase>()
                assertEquals(listOf(request), useCases.observeManualReadoutRequests().toList())
                assertEquals(listOf(request), useCases.observeManualReadoutHotkeyPresses().toList())
                assertEquals(listOf(sets), useCases.observeManualReadoutSets().toList())
                verify(exactly = 1) { setRepository.observeSets() }
                verify(exactly = 1) { requestRepository.observeRequests() }
                verify(exactly = 1) { manualHotkeyRepository.pressStream() }
                confirmVerified(requestRepository, setRepository, manualHotkeyRepository)
            } finally {
                application.close()
            }
        }
}
