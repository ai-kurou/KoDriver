package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.repository.HotkeyRepository

class ObserveReadoutHotkeyRegistrationStateUseCase(
    private val repository: HotkeyRepository,
) {
    operator fun invoke(): Flow<HotkeyRegistrationState> = repository.registrationStateStream()
}
