package kurou.kodriver.feature.main

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.model.SELECTED_AUDIO_INPUT_DEVICE_NAME_UNSPECIFIED
import kurou.kodriver.domain.repository.AudioInputDeviceRepository
import kurou.kodriver.domain.repository.MicrophoneRepository
import kurou.kodriver.domain.repository.SelectedAudioInputDevicePreferencesRepository
import kurou.kodriver.domain.usecase.GetAudioInputDevicesUseCase
import kurou.kodriver.domain.usecase.ObserveMicrophoneInputStatusUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedAudioInputDeviceNameUseCase
import kurou.kodriver.domain.usecase.SaveSelectedAudioInputDeviceNameUseCase

/** 選択したマイク名をメモリ上に保持するテスト用の Repository。 */
internal class FakeSelectedAudioInputDevicePreferencesRepository(
    initial: String = SELECTED_AUDIO_INPUT_DEVICE_NAME_UNSPECIFIED,
) : SelectedAudioInputDevicePreferencesRepository {
    val saved = MutableStateFlow(initial)

    override fun selectedDeviceName(): Flow<String> = saved

    override suspend fun saveSelectedDeviceName(name: String) {
        saved.value = name
    }
}

internal fun microphoneInputViewModel(
    microphoneRepository: MicrophoneRepository,
    deviceRepository: AudioInputDeviceRepository,
    selectedRepository: SelectedAudioInputDevicePreferencesRepository =
        FakeSelectedAudioInputDevicePreferencesRepository(),
) = MicrophoneInputViewModel(
    observeMicrophoneInputStatus = ObserveMicrophoneInputStatusUseCase(microphoneRepository),
    getAudioInputDevices = GetAudioInputDevicesUseCase(deviceRepository),
    observeSelectedAudioInputDeviceName = ObserveSelectedAudioInputDeviceNameUseCase(selectedRepository),
    saveSelectedAudioInputDeviceName = SaveSelectedAudioInputDeviceNameUseCase(selectedRepository),
)
