package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.SelectedAudioInputDevicePreferencesRepository

class SaveSelectedAudioInputDeviceNameUseCase(
    private val repository: SelectedAudioInputDevicePreferencesRepository,
) {
    suspend operator fun invoke(name: String) = repository.saveSelectedDeviceName(name)
}
