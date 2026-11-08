package kurou.kodriver.feature.main

import androidx.lifecycle.viewModelScope
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.model.SpeechRecognitionMode
import kurou.kodriver.domain.repository.AudioInputDeviceRepository
import kurou.kodriver.domain.repository.ManualReadoutItemRequestRepository
import kurou.kodriver.domain.repository.RecordAudioPermissionRepository
import kurou.kodriver.domain.repository.SelectedAudioInputDevicePreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.SpeechPlaybackStateRepository
import kurou.kodriver.domain.repository.SpeechRecognitionRepository
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SpeechRecognitionModuleTest {
    private val speechRepository: SpeechRecognitionRepository = mockk()
    private val permissionRepository: RecordAudioPermissionRepository = mockk()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `解決したViewModelはRepositoryを使って聞き取りを開始する`() {
        every { permissionRepository.isGranted() } returns true
        every { speechRepository.observeRecognition() } returns emptyFlow()
        every { speechRepository.setMode(SpeechRecognitionMode.WakeWord) } returns Unit
        val application =
            koinApplication {
                modules(
                    mainModule,
                    module {
                        single<SpeechRecognitionRepository> { speechRepository }
                        single<RecordAudioPermissionRepository> { permissionRepository }
                        single<ManualReadoutItemRequestRepository> { FakeManualReadoutItemRequestRepository() }
                        single<SpeechPlaybackStateRepository> { FakeSpeechPlaybackStateRepository() }
                        single<SimulatorPreferencesRepository> { FakeSelectedSimulatorRepository() }
                    },
                )
            }
        try {
            val viewModel = application.koin.get<SpeechRecognitionViewModel>()
            try {
                assertEquals(SpeechRecognitionUiState(), viewModel.uiState.value)
                viewModel.refreshDevices()
                assertEquals(SpeechRecognitionUiState(), viewModel.uiState.value)
                viewModel.onEnabledChange(true) { error("権限は要求しない") }
                assertEquals(SpeechRecognitionStatus.Starting, viewModel.uiState.value.status)
                verify(exactly = 1) { permissionRepository.isGranted() }
                verify(exactly = 1) { speechRepository.observeRecognition() }
                verify(exactly = 1) { speechRepository.setMode(SpeechRecognitionMode.WakeWord) }
                confirmVerified(permissionRepository, speechRepository)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        } finally {
            application.close()
        }
    }

    @Test
    fun `入力デバイスのRepositoryを登録していればViewModelは入力デバイスの一覧を取得する`() {
        val deviceRepository = FakeAudioInputDeviceRepository(listOf(AudioInputDevice("Mic A")))
        val application =
            koinApplication {
                modules(
                    mainModule,
                    module {
                        single<SpeechRecognitionRepository> { speechRepository }
                        single<RecordAudioPermissionRepository> { permissionRepository }
                        single<ManualReadoutItemRequestRepository> { FakeManualReadoutItemRequestRepository() }
                        single<SpeechPlaybackStateRepository> { FakeSpeechPlaybackStateRepository() }
                        single<SimulatorPreferencesRepository> { FakeSelectedSimulatorRepository() }
                        single<AudioInputDeviceRepository> { deviceRepository }
                        single<SelectedAudioInputDevicePreferencesRepository> {
                            FakeSelectedAudioInputDevicePreferencesRepository()
                        }
                    },
                )
            }
        try {
            val viewModel = application.koin.get<SpeechRecognitionViewModel>()
            try {
                viewModel.refreshDevices()
                assertEquals(listOf("Mic A"), viewModel.uiState.value.devices)
                assertEquals("Mic A", viewModel.uiState.value.deviceName)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        } finally {
            application.close()
        }
    }
}
