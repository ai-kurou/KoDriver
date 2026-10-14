package kurou.kodriver.feature.lmuwindowsnarrator

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.ManualReadoutRequestRepository
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutHotkeyUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class LmuWindowsNarratorModuleTest {
    private val setRepository: ManualReadoutSetPreferencesRepository = mockk()
    private val requestRepository: ManualReadoutRequestRepository = mockk()
    private val observeEnergy: ObserveLmuWindowsVirtualEnergyUseCase = mockk()
    private val observeHotkey: ObserveReadoutHotkeyUseCase = mockk()
    private val observeThreshold: ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase = mockk()

    @Test
    fun `仮想エネルギーのUseCase群は手動依頼とLMUのセットRepositoryも解決する`() =
        runTest {
            val request = ManualReadoutRequest(Simulator.LmuWindows.id, "set-1")
            val sets = listOf(ManualReadoutSet("set-1"))
            every { requestRepository.observeRequests() } returns flowOf(request)
            every { setRepository.observeSets() } returns flowOf(sets)
            val application =
                koinApplication {
                    modules(
                        lmuWindowsNarratorModule,
                        module {
                            single<ManualReadoutRequestRepository> { requestRepository }
                            single<ManualReadoutSetPreferencesRepository>(named(Simulator.LmuWindows.id)) {
                                setRepository
                            }
                            factory { observeEnergy }
                            factory { observeHotkey }
                            factory { observeThreshold }
                        },
                    )
                }
            try {
                val useCases = application.koin.get<RemainingVirtualEnergyUseCases>()
                assertSame(observeEnergy, useCases.observeRemainingVirtualEnergy)
                assertSame(observeHotkey, useCases.observeReadoutHotkey)
                assertSame(observeThreshold, useCases.observeThresholdPercentage)
                assertEquals(listOf(request), useCases.observeManualReadoutRequests().toList())
                assertEquals(listOf(sets), useCases.observeManualReadoutSets().toList())
                verify(exactly = 1) { setRepository.observeSets() }
                verify(exactly = 1) { requestRepository.observeRequests() }
                confirmVerified(requestRepository, setRepository)
            } finally {
                application.close()
            }
        }
}
