package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.HotkeyRepository

public class ObserveReadoutHotkeyUseCase(
    private val repository: HotkeyRepository,
) {
    public operator fun invoke(): Flow<Unit> = repository.hotkeyPressStream()
}
