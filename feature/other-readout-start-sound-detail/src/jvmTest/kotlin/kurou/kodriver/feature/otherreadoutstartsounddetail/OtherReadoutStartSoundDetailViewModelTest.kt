package kurou.kodriver.feature.otherreadoutstartsounddetail

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
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.PreviewStartSoundUseCase
import kurou.kodriver.domain.usecase.SaveReadoutStartSoundTypeUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherReadoutStartSoundDetailViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: ReadoutStartSoundPreferencesRepository

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

    private val typeFlow = MutableStateFlow(ReadoutStartSoundType.FORMULA_RADIO)

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        OtherReadoutStartSoundDetailViewModel(
        observeReadoutStartSoundType = ObserveReadoutStartSoundTypeUseCase(repository),
        saveReadoutStartSoundType = SaveReadoutStartSoundTypeUseCase(repository),
        previewStartSound = PreviewStartSoundUseCase(ttsEngine),
    )

    @Test
    fun `保存済みの種別を UiState で返す`() =
        runTest(dispatcher) {
        every { repository.observeType() } returns typeFlow

        val viewModel = createViewModel()
        val state = viewModel.uiState.first()

        assertEquals(ReadoutStartSoundType.FORMULA_RADIO, state.selectedType)
        assertEquals(ReadoutStartSoundType.FORMULA_RADIO, state.pendingType)
        verify(exactly = 1) { repository.observeType() }
        confirmVerified(repository, ttsEngine)
    }

    @Test
    fun `onPendingTypeSelected で pendingType が更新されプレビュー再生される`() =
        runTest(dispatcher) {
        every { repository.observeType() } returns typeFlow
        every { ttsEngine.previewStartSound(ReadoutStartSoundType.ELECTRONIC_NOISE) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPendingTypeSelected(ReadoutStartSoundType.ELECTRONIC_NOISE)

        val state = viewModel.uiState.first()
        assertEquals(ReadoutStartSoundType.ELECTRONIC_NOISE, state.pendingType)
        assertEquals(ReadoutStartSoundType.FORMULA_RADIO, state.selectedType)
        verify(exactly = 1) { repository.observeType() }
        verify(exactly = 1) { ttsEngine.previewStartSound(ReadoutStartSoundType.ELECTRONIC_NOISE) }
        confirmVerified(repository, ttsEngine)
    }

    @Test
    fun `onConfirm で pendingType がリポジトリに保存される`() =
        runTest(dispatcher) {
        every { repository.observeType() } returns typeFlow
        every { ttsEngine.previewStartSound(ReadoutStartSoundType.ELECTRONIC_NOISE) } returns Unit
        coEvery { repository.saveType(ReadoutStartSoundType.ELECTRONIC_NOISE) } answers {
            typeFlow.update { ReadoutStartSoundType.ELECTRONIC_NOISE }
        }
        val viewModel = createViewModel()

        viewModel.onPendingTypeSelected(ReadoutStartSoundType.ELECTRONIC_NOISE)
        viewModel.onConfirm()

        val state = viewModel.uiState.first()
        assertEquals(ReadoutStartSoundType.ELECTRONIC_NOISE, state.selectedType)
        assertEquals(ReadoutStartSoundType.ELECTRONIC_NOISE, state.pendingType)
        verify(exactly = 1) { repository.observeType() }
        verify(exactly = 1) { ttsEngine.previewStartSound(ReadoutStartSoundType.ELECTRONIC_NOISE) }
        coVerify(exactly = 1) { repository.saveType(ReadoutStartSoundType.ELECTRONIC_NOISE) }
        confirmVerified(repository, ttsEngine)
    }

    @Test
    fun `onConfirm は pendingType が未選択のとき何もしない`() =
        runTest(dispatcher) {
        every { repository.observeType() } returns typeFlow
        val viewModel = createViewModel()

        viewModel.onConfirm()

        val state = viewModel.uiState.first()
        assertEquals(ReadoutStartSoundType.FORMULA_RADIO, state.selectedType)
        verify(exactly = 1) { repository.observeType() }
        confirmVerified(repository, ttsEngine)
    }

    @Test
    fun `onDismiss で pendingType がリセットされる`() =
        runTest(dispatcher) {
        every { repository.observeType() } returns typeFlow
        every { ttsEngine.previewStartSound(ReadoutStartSoundType.ELECTRONIC_NOISE) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPendingTypeSelected(ReadoutStartSoundType.ELECTRONIC_NOISE)
        viewModel.onDismiss()

        val state = viewModel.uiState.first()
        assertEquals(ReadoutStartSoundType.FORMULA_RADIO, state.pendingType)
        verify(exactly = 1) { repository.observeType() }
        verify(exactly = 1) { ttsEngine.previewStartSound(ReadoutStartSoundType.ELECTRONIC_NOISE) }
        confirmVerified(repository, ttsEngine)
    }

    @Test
    fun `リポジトリの種別が変わると selectedType に反映される`() =
        runTest(dispatcher) {
        every { repository.observeType() } returns typeFlow
        val viewModel = createViewModel()

        typeFlow.update { ReadoutStartSoundType.ELECTRONIC_NOISE }

        val state = viewModel.uiState.first()
        assertEquals(ReadoutStartSoundType.ELECTRONIC_NOISE, state.selectedType)
        verify(exactly = 1) { repository.observeType() }
        confirmVerified(repository, ttsEngine)
    }
}
