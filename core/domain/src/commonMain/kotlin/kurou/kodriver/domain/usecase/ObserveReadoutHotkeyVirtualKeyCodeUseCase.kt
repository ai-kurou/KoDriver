package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.HotkeyRepository

class ObserveReadoutHotkeyVirtualKeyCodeUseCase(
    private val repository: HotkeyRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observeVirtualKeyCode()
}
