@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.othervolumedetail

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
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveSoundVolumeUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherVolumeDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: SoundVolumePreferencesRepository

    private val volumeFlow = MutableStateFlow(80)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = OtherVolumeDetailViewModel(
        observeSoundVolume = ObserveSoundVolumeUseCase(repository),
        saveSoundVolume = SaveSoundVolumeUseCase(repository),
    )

    @Test
    fun `保存済みの音量をUiStateで返す`() = runTest {
        every { repository.volume() } returns volumeFlow
        val viewModel = createViewModel()

        assertEquals(OtherVolumeDetailUiState(volume = 80), viewModel.uiState.first())
        verify(exactly = 1) { repository.volume() }
        confirmVerified(repository)
    }

    @Test
    fun `音量を変更するとUiStateが更新される`() = runTest {
        every { repository.volume() } returns volumeFlow
        coEvery { repository.saveVolume(40) } answers { volumeFlow.update { 40 } }
        val viewModel = createViewModel()

        viewModel.onVolumeChanged(40)

        assertEquals(OtherVolumeDetailUiState(volume = 40), viewModel.uiState.first())
        verify(exactly = 1) { repository.volume() }
        coVerify(exactly = 1) { repository.saveVolume(40) }
        confirmVerified(repository)
    }
}
