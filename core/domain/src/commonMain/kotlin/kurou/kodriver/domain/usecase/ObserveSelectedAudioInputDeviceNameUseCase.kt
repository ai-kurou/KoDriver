package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.SelectedAudioInputDevicePreferencesRepository

public class ObserveSelectedAudioInputDeviceNameUseCase(
    private val repository: SelectedAudioInputDevicePreferencesRepository,
) {
    public operator fun invoke(): Flow<String> = repository.selectedDeviceName()
}
