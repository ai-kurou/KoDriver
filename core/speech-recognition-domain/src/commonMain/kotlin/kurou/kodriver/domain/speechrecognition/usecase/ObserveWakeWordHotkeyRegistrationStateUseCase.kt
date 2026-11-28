package kurou.kodriver.domain.speechrecognition.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyRepository

public class ObserveWakeWordHotkeyRegistrationStateUseCase(
    private val repository: WakeWordHotkeyRepository,
) {
    public operator fun invoke(): Flow<HotkeyRegistrationState> = repository.registrationStateStream()
}
