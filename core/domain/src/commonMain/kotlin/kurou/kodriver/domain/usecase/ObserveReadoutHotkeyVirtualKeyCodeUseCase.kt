package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.ReadoutHotkeyPreferencesRepository

class ObserveReadoutHotkeyVirtualKeyCodeUseCase(
    private val repository: ReadoutHotkeyPreferencesRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observeVirtualKeyCode()
}
