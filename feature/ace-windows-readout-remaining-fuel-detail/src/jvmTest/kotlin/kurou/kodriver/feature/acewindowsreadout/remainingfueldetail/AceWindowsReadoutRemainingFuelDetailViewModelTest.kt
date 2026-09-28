@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.acewindowsreadout.remainingfueldetail

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
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsReadoutRemainingFuelDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: AceWindowsRemainingFuelPreferencesRepository = mockk()

    private val readoutPreferencesRepository: ReadoutPreferencesRepository = mockk()

    private val ttsEngine: TextToSpeechEngine = mockk(relaxUnitFun = true)

    private val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns
            enabledStatesFlow
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        AceWindowsReadoutRemainingFuelDetailViewModel(
            observeThresholdPercentage = ObserveAceWindowsRemainingFuelThresholdPercentageUseCase(repository),
            saveThresholdPercentage = SaveAceWindowsRemainingFuelThresholdPercentageUseCase(repository),
            observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutPreferencesRepository),
            saveReadoutEnabledState = SaveReadoutEnabledStateUseCase(readoutPreferencesRepository),
            playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
        )

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映したUiStateを返す`() =
        runTest {
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(30)
            val viewModel = createViewModel()

            assertEquals(
                AceWindowsReadoutRemainingFuelDetailUiState(thresholdPercentage = 30, enabled = true),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `onThresholdChangedを呼ぶとuiStateのthresholdPercentageが更新される`() =
        runTest {
            val thresholdFlow = MutableStateFlow(30)
            every { repository.observeThresholdPercentage() } returns thresholdFlow
            coEvery { repository.saveThresholdPercentage(50) } answers { thresholdFlow.update { 50 } }
            val viewModel = createViewModel()

            viewModel.onThresholdChanged(50)

            assertEquals(50, viewModel.uiState.first().thresholdPercentage)
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) }
            coVerify(exactly = 1) { repository.saveThresholdPercentage(50) }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `onThresholdResetを呼ぶとthresholdPercentageがデフォルト値30に戻る`() =
        runTest {
            val thresholdFlow = MutableStateFlow(30)
            every { repository.observeThresholdPercentage() } returns thresholdFlow
            coEvery { repository.saveThresholdPercentage(50) } answers { thresholdFlow.update { 50 } }
            coEvery { repository.saveThresholdPercentage(30) } answers { thresholdFlow.update { 30 } }
            val viewModel = createViewModel()

            viewModel.onThresholdChanged(50)
            viewModel.onThresholdReset()

            assertEquals(30, viewModel.uiState.first().thresholdPercentage)
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) }
            coVerify(exactly = 1) { repository.saveThresholdPercentage(50) }
            coVerify(exactly = 1) { repository.saveThresholdPercentage(30) }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `onEnabledChangedにfalseを渡すとuiStateのenabledがfalseになる`() =
        runTest {
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(30)
            coEvery {
                readoutPreferencesRepository.saveReadoutEnabledState(
                    Simulator.AceWindows.id,
                    ReadoutItemKey.AceWindows.RemainingFuel.DetailEnabled,
                    false,
                )
            } answers {
                enabledStatesFlow.update { it + (ReadoutItemKey.AceWindows.RemainingFuel.DetailEnabled to false) }
            }
            val viewModel = createViewModel()

            viewModel.onEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().enabled)
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) }
            coVerify(exactly = 1) {
                readoutPreferencesRepository.saveReadoutEnabledState(
                    Simulator.AceWindows.id,
                    ReadoutItemKey.AceWindows.RemainingFuel.DetailEnabled,
                    false,
                )
            }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `onPreviewClickedを呼ぶと残り燃料警告イベントが再生される`() =
        runTest {
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(30)
            val viewModel = createViewModel()

            viewModel.onPreviewClicked()

            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, false) }
            confirmVerified(repository, readoutPreferencesRepository, ttsEngine)
        }
}
