package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import kurou.kodriver.domain.repository.SpeechPlaybackStateRepository

public class ObserveSpeechPlaybackActiveUseCase(
    private val repository: SpeechPlaybackStateRepository,
) {
    public operator fun invoke(): StateFlow<Boolean> = repository.isActive
}
