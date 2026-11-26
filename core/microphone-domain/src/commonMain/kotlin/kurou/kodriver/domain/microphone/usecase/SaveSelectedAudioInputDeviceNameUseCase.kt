package kurou.kodriver.domain.microphone.usecase

import kurou.kodriver.domain.microphone.repository.SelectedAudioInputDevicePreferencesRepository

public class SaveSelectedAudioInputDeviceNameUseCase(
    private val repository: SelectedAudioInputDevicePreferencesRepository,
) {
    public suspend operator fun invoke(name: String): Unit = repository.saveSelectedDeviceName(name)
}
