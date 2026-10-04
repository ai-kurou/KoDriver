package kurou.kodriver.feature.gt7ps5readout.remainingfueldetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5ReadoutRemainingFuelDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: Gt7Ps5RemainingFuelPreferencesRepository = mockk()

    private val readoutPreferencesRepository: ReadoutPreferencesRepository = mockk()

    private val ttsEngine: TextToSpeechEngine = mockk()

    private val thresholdFlow = MutableStateFlow(GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT)

    private val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id) } returns
            enabledStatesFlow
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        Gt7Ps5ReadoutRemainingFuelDetailViewModel(
            remainingFuelUseCases =
                RemainingFuelUseCases(
                    observeThresholdPercentage = ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase(repository),
                    saveThresholdPercentage = SaveGt7Ps5RemainingFuelThresholdPercentageUseCase(repository),
                    observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutPreferencesRepository),
                    saveReadoutEnabledState = SaveReadoutEnabledStateUseCase(readoutPreferencesRepository),
                ),
            playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
        )

    @Test
    fun `初期状態は燃料残量閾値30パーセントのUiStateを返す`() =
        runTest {
            every { repository.observeThresholdPercentage() } returns thresholdFlow
            val viewModel = createViewModel()

            assertEquals(
                GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT,
                viewModel.uiState.first().thresholdPercentage,
            )
            assertEquals(true, viewModel.uiState.first().enabled)
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id) }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `onThresholdChangedに45を渡すと燃料残量閾値が45パーセントになる`() =
        runTest {
            every { repository.observeThresholdPercentage() } returns thresholdFlow
            coEvery { repository.saveThresholdPercentage(45) } answers { thresholdFlow.update { 45 } }
            val viewModel = createViewModel()

            viewModel.onThresholdChanged(45)

            assertEquals(45, viewModel.uiState.first().thresholdPercentage)
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id) }
            coVerify(exactly = 1) { repository.saveThresholdPercentage(45) }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `onThresholdResetを呼ぶと燃料残量閾値が30パーセントになる`() =
        runTest {
            thresholdFlow.update { 60 }
            every { repository.observeThresholdPercentage() } returns thresholdFlow
            coEvery {
                repository.saveThresholdPercentage(GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT)
            } answers {
                thresholdFlow.update { GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT }
            }
            val viewModel = createViewModel()

            viewModel.onThresholdReset()

            assertEquals(
                GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT,
                viewModel.uiState.first().thresholdPercentage,
            )
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id) }
            coVerify(exactly = 1) {
                repository.saveThresholdPercentage(GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT)
            }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `onEnabledChangedにfalseを渡すとuiStateのenabledがfalseになる`() =
        runTest {
            every { repository.observeThresholdPercentage() } returns thresholdFlow
            coEvery {
                readoutPreferencesRepository.saveReadoutEnabledState(
                    Simulator.Gt7Ps5.id,
                    ReadoutItemKey.Gt7Ps5.RemainingFuel.DetailEnabled,
                    false,
                )
            } answers {
                enabledStatesFlow.update { it + (ReadoutItemKey.Gt7Ps5.RemainingFuel.DetailEnabled to false) }
            }
            val viewModel = createViewModel()

            viewModel.onEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().enabled)
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id) }
            coVerify(exactly = 1) {
                readoutPreferencesRepository.saveReadoutEnabledState(
                    Simulator.Gt7Ps5.id,
                    ReadoutItemKey.Gt7Ps5.RemainingFuel.DetailEnabled,
                    false,
                )
            }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `onPreviewClickedを呼ぶと燃料残量警告を読み上げる`() =
        runTest {
            thresholdFlow.update { 45 }
            every { repository.observeThresholdPercentage() } returns thresholdFlow
            every { ttsEngine.speak(SpeechEvent.Gt7Ps5RemainingFuelWarning(45), false) } returns Unit
            val viewModel = createViewModel()
            viewModel.uiState.first()

            viewModel.onPreviewClicked()

            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id) }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.Gt7Ps5RemainingFuelWarning(45), false) }
            confirmVerified(repository, readoutPreferencesRepository, ttsEngine)
        }
}
