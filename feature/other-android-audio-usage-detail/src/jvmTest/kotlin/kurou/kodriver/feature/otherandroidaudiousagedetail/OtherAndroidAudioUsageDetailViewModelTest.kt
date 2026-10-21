package kurou.kodriver.feature.otherandroidaudiousagedetail

import androidx.lifecycle.viewModelScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.domain.repository.AndroidAudioUsagePreferencesRepository
import kurou.kodriver.domain.usecase.ObserveAndroidAudioUsageUseCase
import kurou.kodriver.domain.usecase.SaveAndroidAudioUsageUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherAndroidAudioUsageDetailViewModelTest {
    private val repository: AndroidAudioUsagePreferencesRepository = mockk()
    private val usageFlow = MutableStateFlow(AndroidAudioUsage.ALARM)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `購読前は既定値で購読後は保存済み用途と変更を表示する`() =
        runTest {
            every { repository.observeAndroidAudioUsage() } returns usageFlow
            val viewModel = createViewModel()
            try {
                assertEquals(OtherAndroidAudioUsageDetailUiState(), viewModel.uiState.value)
                viewModel.uiState.launchIn(backgroundScope)
                runCurrent()
                assertEquals(AndroidAudioUsage.ALARM, viewModel.uiState.value.usage)
                usageFlow.update { AndroidAudioUsage.COMMUNICATION }
                runCurrent()
                assertEquals(AndroidAudioUsage.COMMUNICATION, viewModel.uiState.value.usage)
                verify(exactly = 1) { repository.observeAndroidAudioUsage() }
                confirmVerified(repository)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `全用途の選択を保存して監視結果に反映する`() =
        runTest {
            every { repository.observeAndroidAudioUsage() } returns usageFlow
            AndroidAudioUsage.entries.forEach { usage ->
                coEvery { repository.saveAndroidAudioUsage(usage) } answers { usageFlow.update { usage } }
            }
            val viewModel = createViewModel()
            try {
                viewModel.uiState.launchIn(backgroundScope)
                runCurrent()
                AndroidAudioUsage.entries.forEach { usage ->
                    viewModel.onUsageSelected(usage)
                    runCurrent()
                    assertEquals(usage, viewModel.uiState.value.usage)
                    coVerify(exactly = 1) { repository.saveAndroidAudioUsage(usage) }
                }
                verify(exactly = 1) { repository.observeAndroidAudioUsage() }
                confirmVerified(repository)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    private fun createViewModel() =
        OtherAndroidAudioUsageDetailViewModel(
            ObserveAndroidAudioUsageUseCase(repository),
            SaveAndroidAudioUsageUseCase(repository),
        )
}
