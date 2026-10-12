package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.HotkeyRepository

class ObserveReadoutHotkeyUseCase(
    private val repository: HotkeyRepository,
) {
    operator fun invoke(): Flow<Unit> = repository.hotkeyPressStream()
}
