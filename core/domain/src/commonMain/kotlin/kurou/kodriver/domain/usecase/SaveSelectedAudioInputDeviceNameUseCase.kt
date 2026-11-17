package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.SelectedAudioInputDevicePreferencesRepository

public class SaveSelectedAudioInputDeviceNameUseCase(
    private val repository: SelectedAudioInputDevicePreferencesRepository,
) {
    public suspend operator fun invoke(name: String): Unit = repository.saveSelectedDeviceName(name)
}
