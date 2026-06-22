@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.usecase.ObserveMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveMyBestLapVoiceTypeUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5ReadoutMyBestLapDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeMyBestLapPreferencesRepository
    private lateinit var viewModel: Gt7Ps5ReadoutMyBestLapDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeMyBestLapPreferencesRepository()
        viewModel = Gt7Ps5ReadoutMyBestLapDetailViewModel(
            observeMyBestLapVoiceType = ObserveMyBestLapVoiceTypeUseCase(repository),
            saveMyBestLapVoiceType = SaveMyBestLapVoiceTypeUseCase(repository),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態は voiceType=FORMAL の UiState を返す`() = runTest {
        val state = viewModel.uiState.first()
        assertEquals(MyBestLapVoiceType.FORMAL, state.voiceType)
    }

    @Test
    fun `onVoiceTypeChanged に CASUAL を渡すと voiceType=CASUAL になる`() = runTest {
        viewModel.onVoiceTypeChanged(MyBestLapVoiceType.CASUAL)
        assertEquals(MyBestLapVoiceType.CASUAL, viewModel.uiState.first().voiceType)
    }

    @Test
    fun `onVoiceTypeChanged に FORMAL を渡すと voiceType=FORMAL になる`() = runTest {
        repository.saveVoiceType(MyBestLapVoiceType.CASUAL)
        viewModel.onVoiceTypeChanged(MyBestLapVoiceType.FORMAL)
        assertEquals(MyBestLapVoiceType.FORMAL, viewModel.uiState.first().voiceType)
    }
}
