package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.SelectedAudioInputDevicePreferencesRepository

class ObserveSelectedAudioInputDeviceNameUseCase(
    private val repository: SelectedAudioInputDevicePreferencesRepository,
) {
    operator fun invoke() = repository.selectedDeviceName()
}
