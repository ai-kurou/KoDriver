@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.othervolumedetail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveSoundVolumeUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherVolumeDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeSoundVolumePreferencesRepository
    private lateinit var viewModel: OtherVolumeDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeSoundVolumePreferencesRepository(initialVolume = 80)
        viewModel = OtherVolumeDetailViewModel(
            observeSoundVolume = ObserveSoundVolumeUseCase(repository),
            saveSoundVolume = SaveSoundVolumeUseCase(repository),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `保存済みの音量をUiStateで返す`() = runTest {
        assertEquals(OtherVolumeDetailUiState(volume = 80), viewModel.uiState.first())
    }

    @Test
    fun `音量を変更するとUiStateが更新される`() = runTest {
        viewModel.onVolumeChanged(40)

        assertEquals(OtherVolumeDetailUiState(volume = 40), viewModel.uiState.first())
    }
}
