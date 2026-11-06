package kurou.kodriver.feature.main

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.model.SELECTED_AUDIO_INPUT_DEVICE_NAME_UNSPECIFIED
import kurou.kodriver.domain.repository.AudioInputDeviceRepository
import kurou.kodriver.domain.repository.SelectedAudioInputDevicePreferencesRepository
import kurou.kodriver.domain.usecase.GetAudioInputDevicesUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedAudioInputDeviceNameUseCase
import kurou.kodriver.domain.usecase.SaveSelectedAudioInputDeviceNameUseCase

/** 選択したマイク名をメモリ上に保持するテスト用の Repository。 */
internal class FakeSelectedAudioInputDevicePreferencesRepository(
    initial: String = SELECTED_AUDIO_INPUT_DEVICE_NAME_UNSPECIFIED,
    var readError: Throwable? = null,
) : SelectedAudioInputDevicePreferencesRepository {
    val saved = MutableStateFlow(initial)
    var saveError: Throwable? = null

    override fun selectedDeviceName(): Flow<String> = readError?.let { error -> flow { throw error } } ?: saved

    override suspend fun saveSelectedDeviceName(name: String) {
        saveError?.let { throw it }
        saved.value = name
    }
}

/** 入力デバイスの一覧をメモリ上に保持するテスト用の Repository。 */
internal class FakeAudioInputDeviceRepository(
    var devices: List<AudioInputDevice> = emptyList(),
    var error: Throwable? = null,
) : AudioInputDeviceRepository {
    var getCount = 0
        private set

    override suspend fun getDevices(): List<AudioInputDevice> {
        getCount++
        error?.let { throw it }
        return devices
    }
}

internal fun audioInputDeviceSelector(
    deviceRepository: AudioInputDeviceRepository,
    selectedRepository: SelectedAudioInputDevicePreferencesRepository =
        FakeSelectedAudioInputDevicePreferencesRepository(),
) = AudioInputDeviceSelector(
    getAudioInputDevices = GetAudioInputDevicesUseCase(deviceRepository),
    observeSelectedAudioInputDeviceName = ObserveSelectedAudioInputDeviceNameUseCase(selectedRepository),
    saveSelectedAudioInputDeviceName = SaveSelectedAudioInputDeviceNameUseCase(selectedRepository),
)
