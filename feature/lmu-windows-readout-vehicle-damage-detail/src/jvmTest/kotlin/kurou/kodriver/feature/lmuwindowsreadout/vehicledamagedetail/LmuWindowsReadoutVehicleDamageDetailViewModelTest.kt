@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
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
import kurou.kodriver.domain.model.OverheatVoiceType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsOverheatPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.usecase.ObserveLmuWindowsOverheatVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsOverheatVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleDamageEnabledStateUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutVehicleDamageDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: LmuWindowsVehicleDamagePreferencesRepository

    @MockK
    private lateinit var overheatRepository: LmuWindowsOverheatPreferencesRepository

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        LmuWindowsReadoutVehicleDamageDetailViewModel(
            observeEnabledStates = ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(repository),
            observeOverheatVoiceType = ObserveLmuWindowsOverheatVoiceTypeUseCase(overheatRepository),
            saveEnabledState = SaveLmuWindowsVehicleDamageEnabledStateUseCase(repository),
            saveOverheatVoiceType = SaveLmuWindowsOverheatVoiceTypeUseCase(overheatRepository),
            playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
        )

    @Test
    fun `初期状態はリポジトリが空のとき overheatEnabled がデフォルト値 true の UiState を返す`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { overheatRepository.observeVoiceType() } returns MutableStateFlow(OverheatVoiceType.GP2_GP2)
            val viewModel = createViewModel()

            assertEquals(LmuWindowsReadoutVehicleDamageDetailUiState(overheatEnabled = true), viewModel.uiState.first())
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { overheatRepository.observeVoiceType() }
            confirmVerified(repository, overheatRepository)
        }

    @Test
    fun `リポジトリに overheat=false が保存済みのとき overheatEnabled が false の UiState を返す`() =
        runTest {
            every { repository.observeEnabledStates() } returns
                MutableStateFlow(mapOf(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to false))
            every { overheatRepository.observeVoiceType() } returns MutableStateFlow(OverheatVoiceType.GP2_GP2)
            val viewModel = createViewModel()

            assertEquals(
                LmuWindowsReadoutVehicleDamageDetailUiState(overheatEnabled = false),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { overheatRepository.observeVoiceType() }
            confirmVerified(repository, overheatRepository)
        }

    @Test
    fun `onOverheatEnabledChanged を呼ぶと UiState の overheatEnabled が更新される`() =
        runTest {
            val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { repository.observeEnabledStates() } returns enabledStatesFlow
            every { overheatRepository.observeVoiceType() } returns MutableStateFlow(OverheatVoiceType.GP2_GP2)
            coEvery {
                repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, false)
            } answers {
                enabledStatesFlow.update { it + (ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to false) }
            }
            val viewModel = createViewModel()

            viewModel.onOverheatEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().overheatEnabled)
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { overheatRepository.observeVoiceType() }
            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, false)
            }
            confirmVerified(repository, overheatRepository)
        }

    @Test
    fun `リポジトリに STANDARD が保存済みのとき overheatVoiceType が STANDARD の UiState を返す`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { overheatRepository.observeVoiceType() } returns MutableStateFlow(OverheatVoiceType.STANDARD)
            val viewModel = createViewModel()

            assertEquals(
                LmuWindowsReadoutVehicleDamageDetailUiState(overheatVoiceType = OverheatVoiceType.STANDARD),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { overheatRepository.observeVoiceType() }
            confirmVerified(repository, overheatRepository)
        }

    @Test
    fun `onOverheatVoiceTypeChanged を呼ぶと UiState の overheatVoiceType が更新される`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            val voiceTypeFlow = MutableStateFlow(OverheatVoiceType.GP2_GP2)
            every { overheatRepository.observeVoiceType() } returns voiceTypeFlow
            coEvery { overheatRepository.saveVoiceType(OverheatVoiceType.STANDARD) } answers {
                voiceTypeFlow.update { OverheatVoiceType.STANDARD }
            }
            val viewModel = createViewModel()

            viewModel.onOverheatVoiceTypeChanged(OverheatVoiceType.STANDARD)

            assertEquals(OverheatVoiceType.STANDARD, viewModel.uiState.first().overheatVoiceType)
            coVerify(exactly = 1) { overheatRepository.saveVoiceType(OverheatVoiceType.STANDARD) }
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { overheatRepository.observeVoiceType() }
            confirmVerified(repository, overheatRepository)
        }

    @Test
    fun `onPreviewClicked に GP2_GP2 を渡すと Overheating イベントが再生される`() {
        every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
        every { overheatRepository.observeVoiceType() } returns MutableStateFlow(OverheatVoiceType.GP2_GP2)
        every { ttsEngine.speak(SpeechEvent.Overheating, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked(OverheatVoiceType.GP2_GP2)

        verify(exactly = 1) { repository.observeEnabledStates() }
        verify(exactly = 1) { overheatRepository.observeVoiceType() }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.Overheating, false) }
        confirmVerified(repository, overheatRepository, ttsEngine)
    }

    @Test
    fun `onPreviewClicked に STANDARD を渡すと OverheatingStandard イベントが再生される`() {
        every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
        every { overheatRepository.observeVoiceType() } returns MutableStateFlow(OverheatVoiceType.GP2_GP2)
        every { ttsEngine.speak(SpeechEvent.OverheatingStandard, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked(OverheatVoiceType.STANDARD)

        verify(exactly = 1) { repository.observeEnabledStates() }
        verify(exactly = 1) { overheatRepository.observeVoiceType() }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.OverheatingStandard, false) }
        confirmVerified(repository, overheatRepository, ttsEngine)
    }

    @Test
    fun `リポジトリに partDetached=false が保存済みのとき partDetachedEnabled が false の UiState を返す`() =
        runTest {
            every { repository.observeEnabledStates() } returns
                MutableStateFlow(mapOf(ReadoutItemKey.LmuWindows.VehicleDamage.PartDetached to false))
            every { overheatRepository.observeVoiceType() } returns MutableStateFlow(OverheatVoiceType.GP2_GP2)
            val viewModel = createViewModel()

            assertEquals(
                LmuWindowsReadoutVehicleDamageDetailUiState(partDetachedEnabled = false),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { overheatRepository.observeVoiceType() }
            confirmVerified(repository, overheatRepository)
        }

    @Test
    fun `onPartDetachedEnabledChanged を呼ぶと UiState の partDetachedEnabled が更新される`() =
        runTest {
            val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { repository.observeEnabledStates() } returns enabledStatesFlow
            every { overheatRepository.observeVoiceType() } returns MutableStateFlow(OverheatVoiceType.GP2_GP2)
            coEvery {
                repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.PartDetached, false)
            } answers {
                enabledStatesFlow.update { it + (ReadoutItemKey.LmuWindows.VehicleDamage.PartDetached to false) }
            }
            val viewModel = createViewModel()

            viewModel.onPartDetachedEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().partDetachedEnabled)
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { overheatRepository.observeVoiceType() }
            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.PartDetached, false)
            }
            confirmVerified(repository, overheatRepository)
        }

    @Test
    fun `onPartDetachedPreviewClicked を呼ぶと PartDetached イベントが再生される`() {
        every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
        every { overheatRepository.observeVoiceType() } returns MutableStateFlow(OverheatVoiceType.GP2_GP2)
        every { ttsEngine.speak(SpeechEvent.PartDetached, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPartDetachedPreviewClicked()

        verify(exactly = 1) { repository.observeEnabledStates() }
        verify(exactly = 1) { overheatRepository.observeVoiceType() }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.PartDetached, false) }
        confirmVerified(repository, overheatRepository, ttsEngine)
    }
}
