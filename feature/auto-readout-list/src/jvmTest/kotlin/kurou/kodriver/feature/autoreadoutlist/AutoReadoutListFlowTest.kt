package kurou.kodriver.feature.autoreadoutlist

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.AutoReadoutPreferencesRepository
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundEnabledPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ResolveAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveAutoReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveQueueEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveReadoutStartSoundEnabledStateUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AutoReadoutListFlowTest {
    init {
        registerAllReadoutItemKeys()
    }

    private val readoutRepository: AutoReadoutPreferencesRepository = mockk()
    private val queueRepository: QueuePreferencesRepository = mockk()
    private val startSoundRepository: ReadoutStartSoundEnabledPreferencesRepository = mockk()
    private val saveAutoReadoutOrderUseCase: SaveAutoReadoutOrderUseCase = mockk()
    private val saveAutoReadoutEnabledStateUseCase: SaveAutoReadoutEnabledStateUseCase = mockk()
    private val saveQueueEnabledStateUseCase: SaveQueueEnabledStateUseCase = mockk()
    private val saveReadoutStartSoundEnabledStateUseCase: SaveReadoutStartSoundEnabledStateUseCase = mockk()

    @Test
    fun `選択シミュレーターごとの設定とlocalOrder優先を反映する`() =
        runTest {
            val simulator = MutableStateFlow<Simulator>(Simulator.LmuWindows)
            val lmuOrder = MutableStateFlow(listOf(LmuWindowsReadoutItemKey.Flag.Root))
            val lmuEnabled =
                MutableStateFlow(
                    mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.Flag.Root to false),
                )
            val gt7Enabled =
                MutableStateFlow(
                    mapOf<ReadoutItemKey, Boolean>(Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root to true),
                )
            val queueStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            val soundStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { readoutRepository.observeAutoReadoutOrder("lmu_windows") } returns lmuOrder
            every { readoutRepository.observeAutoReadoutOrder("gt7_ps5") } returns MutableStateFlow(emptyList())
            every { readoutRepository.observeAutoReadoutEnabledStates("lmu_windows") } returns lmuEnabled
            every { readoutRepository.observeAutoReadoutEnabledStates("gt7_ps5") } returns gt7Enabled
            every { queueRepository.observeQueueEnabledStates() } returns queueStates
            every { startSoundRepository.observeStartSoundEnabledStates() } returns soundStates
            val localOrder = MutableStateFlow<LocalOrderForTest>(null to emptyList())
            val flow =
                AutoReadoutListFlow(
                    observeSelectedSimulator = simulator,
                    autoReadoutOrderUseCases =
                        AutoReadoutOrderUseCases(
                            ObserveAutoReadoutOrderUseCase(readoutRepository),
                            ResolveAutoReadoutOrderUseCase(),
                            saveAutoReadoutOrderUseCase,
                        ),
                    autoReadoutEnabledUseCases =
                        AutoReadoutEnabledUseCases(
                            ObserveAutoReadoutEnabledStatesUseCase(readoutRepository),
                            saveAutoReadoutEnabledStateUseCase,
                        ),
                    queueUseCases =
                        QueueUseCases(
                            ObserveQueueEnabledStatesUseCase(queueRepository),
                            saveQueueEnabledStateUseCase,
                        ),
                    startSoundUseCases =
                        StartSoundUseCases(
                            ObserveReadoutStartSoundEnabledStatesUseCase(startSoundRepository),
                            saveReadoutStartSoundEnabledStateUseCase,
                        ),
                    localOrder = localOrder,
                    selectedItem = MutableStateFlow(null),
                )

            val initial = flow.uiState.first()
            assertEquals(
                listOf(LmuWindowsReadoutItemKey.Flag.Root) +
                    AutoReadoutListItemType.defaultOrder(Simulator.LmuWindows).drop(1),
                initial.items,
            )
            assertEquals(false, initial.autoReadoutEnabledStates[LmuWindowsReadoutItemKey.Flag.Root])

            localOrder.update { Simulator.LmuWindows to listOf(LmuWindowsReadoutItemKey.VehicleApproach.Root) }
            val local =
                async(UnconfinedTestDispatcher(testScheduler)) {
                    flow.uiState.first { it.items == listOf(LmuWindowsReadoutItemKey.VehicleApproach.Root) }
                }
            assertEquals(listOf(LmuWindowsReadoutItemKey.VehicleApproach.Root), local.await().items)

            simulator.update { Simulator.Gt7Ps5 }
            val switched =
                flow.uiState.first {
                    it.selectedSimulator == Simulator.Gt7Ps5 &&
                        it.autoReadoutEnabledStates[Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root] == true
                }
            assertEquals(Simulator.Gt7Ps5, switched.selectedSimulator)
            assertEquals(AutoReadoutListItemType.defaultOrder(Simulator.Gt7Ps5), switched.items)
            verify(exactly = 2) { readoutRepository.observeAutoReadoutOrder("lmu_windows") }
            verify(exactly = 1) { readoutRepository.observeAutoReadoutOrder("gt7_ps5") }
            verify(exactly = 2) { readoutRepository.observeAutoReadoutEnabledStates("lmu_windows") }
            verify(exactly = 1) { readoutRepository.observeAutoReadoutEnabledStates("gt7_ps5") }
            verify(exactly = 1) { queueRepository.observeQueueEnabledStates() }
            verify(exactly = 1) { startSoundRepository.observeStartSoundEnabledStates() }
            confirmVerified(readoutRepository, queueRepository, startSoundRepository)
        }
}

private typealias LocalOrderForTest = Pair<Simulator?, List<ReadoutItemKey>>
