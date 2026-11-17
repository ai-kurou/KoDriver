package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.repository.HotkeyRepository

public class ObserveReadoutHotkeyRegistrationStateUseCase(
    private val repository: HotkeyRepository,
) {
    public operator fun invoke(): Flow<HotkeyRegistrationState> = repository.registrationStateStream()
}
