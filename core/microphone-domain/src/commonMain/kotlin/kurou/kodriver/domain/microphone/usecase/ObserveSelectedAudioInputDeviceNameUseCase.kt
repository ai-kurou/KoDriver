package kurou.kodriver.domain.microphone.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.microphone.repository.SelectedAudioInputDevicePreferencesRepository

public class ObserveSelectedAudioInputDeviceNameUseCase(
    private val repository: SelectedAudioInputDevicePreferencesRepository,
) {
    public operator fun invoke(): Flow<String> = repository.selectedDeviceName()
}
