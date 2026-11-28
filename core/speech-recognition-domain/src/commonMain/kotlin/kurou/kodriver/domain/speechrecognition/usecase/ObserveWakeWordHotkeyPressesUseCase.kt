package kurou.kodriver.domain.speechrecognition.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyRepository

public class ObserveWakeWordHotkeyPressesUseCase(
    private val repository: WakeWordHotkeyRepository,
) {
    public operator fun invoke(): Flow<Unit> = repository.pressStream()
}
