package kurou.kodriver.feature.manualreadoutdetail

import androidx.lifecycle.viewModelScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.ManualReadoutHotkeyRepository
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ManualReadoutSetDetailModuleTest {
    private val ttsRepository: TextToSpeechRepository = mockk()
    private val volumeRepository: SoundVolumePreferencesRepository = mockk()
    private val playStartSound: PlayStartSoundForKeyUseCase = mockk()
    private val speakText: SpeakTextUseCase = mockk()
    private val hotkeyRepository: ManualReadoutHotkeyRepository = mockk()
    private val dispatcher = UnconfinedTestDispatcher()
    private val lmuRepository: ManualReadoutSetPreferencesRepository = mockk()
    private val gt7Repository: ManualReadoutSetPreferencesRepository = mockk()
    private val aceRepository: ManualReadoutSetPreferencesRepository = mockk()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `全シミュレーターのパラメータで解決したViewModelは対応するRepositoryのセットを返す`() =
        runTest(dispatcher) {
            coEvery { ttsRepository.isAvailable() } returns true
            val repositories =
                mapOf(
                    Simulator.LmuWindows.id to lmuRepository,
                    Simulator.Gt7Ps5.id to gt7Repository,
                    Simulator.AceWindows.id to aceRepository,
                )
            repositories.forEach { (id, repository) ->
                every { repository.observeSets() } returns flowOf(listOf(ManualReadoutSet(id = id)))
            }
            Simulator.entries.forEach { simulator ->
                every { hotkeyRepository.registrationStateStream(simulator.id, simulator.id) } returns
                    MutableStateFlow(HotkeyRegistrationState.Unsupported)
            }
            val application =
                koinApplication {
                    modules(
                        manualReadoutSetDetailModule,
                        module {
                            single<TextToSpeechRepository> { ttsRepository }
                            single<SoundVolumePreferencesRepository> { volumeRepository }
                            Simulator.entries.forEach { simulator ->
                                single<PlayStartSoundForKeyUseCase>(named(simulator.id)) { playStartSound }
                                single<SpeakTextUseCase>(named(simulator.id)) { speakText }
                            }
                            single<ManualReadoutHotkeyRepository> { hotkeyRepository }
                            repositories.forEach { (id, repository) ->
                                single<ManualReadoutSetPreferencesRepository>(named(id)) { repository }
                            }
                        },
                    )
                }
            try {
                Simulator.entries.forEach { simulator ->
                    val viewModel =
                        application.koin.get<ManualReadoutSetDetailViewModel> {
                            parametersOf(simulator.id, simulator.id)
                        }
                    val repository = repositories.getValue(simulator.id)
                    coEvery { repository.deleteSet(simulator.id) } returns Unit
                    try {
                        val state = viewModel.uiState.first { !it.isLoading }
                        assertEquals(1, state.setNumber)
                        assertEquals(emptyList(), state.selectedItemKeys)
                        assertEquals(manualReadoutItemKeys(simulator.id), state.availableItemKeys)
                        viewModel.deleteSet()
                        assertEquals(true, viewModel.uiState.first { it.deleted }.deleted)
                        coVerify(exactly = 1) { repository.deleteSet(simulator.id) }
                    } finally {
                        viewModel.viewModelScope.cancel()
                    }
                }
                repositories.values.forEach { repository ->
                    verify(exactly = 1) { repository.observeSets() }
                }
                confirmVerified(lmuRepository, gt7Repository, aceRepository)
            } finally {
                application.close()
            }
        }
}
