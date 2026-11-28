package kurou.kodriver.domain.speechrecognition.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.speechrecognition.model.WakeWordHotkey
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyPreferencesRepository

public class ObserveWakeWordHotkeyUseCase(
    private val repository: WakeWordHotkeyPreferencesRepository,
) {
    public operator fun invoke(): Flow<WakeWordHotkey> = repository.observeWakeWordHotkey()
}
