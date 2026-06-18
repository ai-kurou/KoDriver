@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.usecase.ObserveVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveVehicleDamageEnabledStateUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeTextToSpeechEngine(
    private val onSpeak: (SpeechEvent) -> Unit,
) : TextToSpeechEngine {
    override val currentReadoutItemKey: String? = null
    override fun speak(event: SpeechEvent, queue: Boolean) = onSpeak(event)
    override fun stop() = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutVehicleDamageDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeVehicleDamagePreferencesRepository
    private val playedEvents = mutableListOf<SpeechEvent>()
    private lateinit var viewModel: LmuWindowsReadoutVehicleDamageDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeVehicleDamagePreferencesRepository()
        viewModel = LmuWindowsReadoutVehicleDamageDetailViewModel(
            observeEnabledStates = ObserveVehicleDamageEnabledStatesUseCase(repository),
            saveEnabledState = SaveVehicleDamageEnabledStateUseCase(repository),
            playSpeechEvent = PlaySpeechEventUseCase(FakeTextToSpeechEngine { playedEvents.add(it) }),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態はリポジトリが空のとき overheatEnabled がデフォルト値 true の UiState を返す`() = runTest {
        assertEquals(LmuWindowsReadoutVehicleDamageDetailUiState(overheatEnabled = true), viewModel.uiState.first())
    }

    @Test
    fun `リポジトリに overheat=false が保存済みのとき overheatEnabled が false の UiState を返す`() = runTest {
        val repo = FakeVehicleDamagePreferencesRepository(
            initialStates = mapOf(ReadoutItemKey.OVERHEAT to false),
        )
        val vm = LmuWindowsReadoutVehicleDamageDetailViewModel(
            observeEnabledStates = ObserveVehicleDamageEnabledStatesUseCase(repo),
            saveEnabledState = SaveVehicleDamageEnabledStateUseCase(repo),
            playSpeechEvent = PlaySpeechEventUseCase(FakeTextToSpeechEngine {}),
        )

        assertEquals(LmuWindowsReadoutVehicleDamageDetailUiState(overheatEnabled = false), vm.uiState.first())
    }

    @Test
    fun `onOverheatEnabledChanged を呼ぶと UiState の overheatEnabled が更新される`() = runTest {
        viewModel.onOverheatEnabledChanged(false)
        assertEquals(false, viewModel.uiState.first().overheatEnabled)

        viewModel.onOverheatEnabledChanged(true)
        assertEquals(true, viewModel.uiState.first().overheatEnabled)
    }

    @Test
    fun `onPreviewClicked を呼ぶと Overheating イベントが再生される`() {
        viewModel.onPreviewClicked()
        assertEquals(listOf<SpeechEvent>(SpeechEvent.Overheating), playedEvents)
    }
}
