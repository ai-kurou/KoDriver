package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.ReadoutHotkeyPreferencesRepository

public class ObserveReadoutHotkeyVirtualKeyCodeUseCase(
    private val repository: ReadoutHotkeyPreferencesRepository,
) {
    public operator fun invoke(): Flow<Int> = repository.observeVirtualKeyCode()
}
