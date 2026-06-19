@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherreadoutstartsounddetail

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
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.PreviewStartSoundUseCase
import kurou.kodriver.domain.usecase.SaveReadoutStartSoundTypeUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherReadoutStartSoundDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeReadoutStartSoundRepository
    private lateinit var viewModel: OtherReadoutStartSoundDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeReadoutStartSoundRepository(initialType = ReadoutStartSoundType.FORMULA_RADIO)
        viewModel = buildViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        repo: FakeReadoutStartSoundRepository = repository,
    ) = OtherReadoutStartSoundDetailViewModel(
        observeReadoutStartSoundType = ObserveReadoutStartSoundTypeUseCase(repo),
        saveReadoutStartSoundType = SaveReadoutStartSoundTypeUseCase(repo),
        previewStartSound = PreviewStartSoundUseCase(FakeTextToSpeechEngine()),
    )

    @Test
    fun `保存済みの種別を UiState で返す`() = runTest {
        val state = viewModel.uiState.first()

        assertEquals(ReadoutStartSoundType.FORMULA_RADIO, state.selectedType)
        assertEquals(ReadoutStartSoundType.FORMULA_RADIO, state.pendingType)
    }

    @Test
    fun `onPendingTypeSelected で pendingType が更新される`() = runTest {
        viewModel.onPendingTypeSelected(ReadoutStartSoundType.ELECTRONIC_NOISE)

        val state = viewModel.uiState.first()
        assertEquals(ReadoutStartSoundType.ELECTRONIC_NOISE, state.pendingType)
        assertEquals(ReadoutStartSoundType.FORMULA_RADIO, state.selectedType)
    }

    @Test
    fun `onConfirm で pendingType がリポジトリに保存される`() = runTest {
        viewModel.onPendingTypeSelected(ReadoutStartSoundType.ELECTRONIC_NOISE)
        viewModel.onConfirm()

        val state = viewModel.uiState.first()
        assertEquals(ReadoutStartSoundType.ELECTRONIC_NOISE, state.selectedType)
        assertEquals(ReadoutStartSoundType.ELECTRONIC_NOISE, state.pendingType)
    }

    @Test
    fun `onConfirm は pendingType が未選択のとき何もしない`() = runTest {
        viewModel.onConfirm()

        val state = viewModel.uiState.first()
        assertEquals(ReadoutStartSoundType.FORMULA_RADIO, state.selectedType)
    }

    @Test
    fun `onDismiss で pendingType がリセットされる`() = runTest {
        viewModel.onPendingTypeSelected(ReadoutStartSoundType.ELECTRONIC_NOISE)
        viewModel.onDismiss()

        val state = viewModel.uiState.first()
        assertEquals(ReadoutStartSoundType.FORMULA_RADIO, state.pendingType)
    }

    @Test
    fun `リポジトリの種別が変わると selectedType に反映される`() = runTest {
        repository.saveType(ReadoutStartSoundType.ELECTRONIC_NOISE)

        val state = viewModel.uiState.first()
        assertEquals(ReadoutStartSoundType.ELECTRONIC_NOISE, state.selectedType)
    }
}

private class FakeTextToSpeechEngine : TextToSpeechEngine {
    override val currentReadoutItemKey: ReadoutItemKey? = null
    override fun speak(event: SpeechEvent, queue: Boolean) = Unit
    override fun stop() = Unit
    override fun previewStartSound(type: ReadoutStartSoundType) = Unit
}
