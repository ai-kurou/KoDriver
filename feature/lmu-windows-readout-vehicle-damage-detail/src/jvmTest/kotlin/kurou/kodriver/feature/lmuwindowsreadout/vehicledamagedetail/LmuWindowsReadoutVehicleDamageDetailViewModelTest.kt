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
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleDamageEnabledStateUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutVehicleDamageDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: LmuWindowsVehicleDamagePreferencesRepository

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = LmuWindowsReadoutVehicleDamageDetailViewModel(
        observeEnabledStates = ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(repository),
        saveEnabledState = SaveLmuWindowsVehicleDamageEnabledStateUseCase(repository),
        playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
    )

    @Test
    fun `初期状態はリポジトリが空のとき overheatEnabled がデフォルト値 true の UiState を返す`() = runTest {
        every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
        val viewModel = createViewModel()

        assertEquals(LmuWindowsReadoutVehicleDamageDetailUiState(overheatEnabled = true), viewModel.uiState.first())
        verify(exactly = 1) { repository.observeEnabledStates() }
        confirmVerified(repository)
    }

    @Test
    fun `リポジトリに overheat=false が保存済みのとき overheatEnabled が false の UiState を返す`() = runTest {
        every { repository.observeEnabledStates() } returns
            MutableStateFlow(mapOf(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to false))
        val viewModel = createViewModel()

        assertEquals(LmuWindowsReadoutVehicleDamageDetailUiState(overheatEnabled = false), viewModel.uiState.first())
        verify(exactly = 1) { repository.observeEnabledStates() }
        confirmVerified(repository)
    }

    @Test
    fun `onOverheatEnabledChanged を呼ぶと UiState の overheatEnabled が更新される`() = runTest {
        val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
        every { repository.observeEnabledStates() } returns enabledStatesFlow
        coEvery {
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, false)
        } answers {
            enabledStatesFlow.update { it + (ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to false) }
        }
        val viewModel = createViewModel()

        viewModel.onOverheatEnabledChanged(false)

        assertEquals(false, viewModel.uiState.first().overheatEnabled)
        verify(exactly = 1) { repository.observeEnabledStates() }
        coVerify(exactly = 1) {
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, false)
        }
        confirmVerified(repository)
    }

    @Test
    fun `onPreviewClicked を呼ぶと Overheating イベントが再生される`() {
        every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
        every { ttsEngine.speak(SpeechEvent.Overheating, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked()

        verify(exactly = 1) { repository.observeEnabledStates() }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.Overheating, false) }
        confirmVerified(repository, ttsEngine)
    }
}
