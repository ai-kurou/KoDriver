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
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kurou.kodriver.domain.repository.VoicePitchPreferencesRepository
import kurou.kodriver.domain.repository.VoicePreferencesRepository
import kurou.kodriver.domain.repository.VoiceSpeedPreferencesRepository
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherAndroidAudioUsageDetailModuleTest {
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

    private fun createViewModel(): OtherAndroidAudioUsageDetailViewModel {
        val application =
            koinApplication {
                modules(
                    otherAndroidAudioUsageDetailModule,
                    module { single<AndroidAudioUsagePreferencesRepository> { repository } },
                    module {
                        single<SoundVolumePreferencesRepository> { mockk() }
                        single<TextToSpeechRepository> { mockk() }
                        single<VoicePreferencesRepository> { mockk() }
                        single<VoiceSpeedPreferencesRepository> { mockk() }
                        single<VoicePitchPreferencesRepository> { mockk() }
                    },
                )
            }
        return try {
            application.koin.get<OtherAndroidAudioUsageDetailViewModel>()
        } finally {
            application.close()
        }
    }
}
