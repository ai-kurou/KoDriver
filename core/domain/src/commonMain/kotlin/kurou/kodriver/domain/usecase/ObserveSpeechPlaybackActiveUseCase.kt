package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import kurou.kodriver.domain.repository.SpeechPlaybackStateRepository

class ObserveSpeechPlaybackActiveUseCase(
    private val repository: SpeechPlaybackStateRepository,
) {
    operator fun invoke(): StateFlow<Boolean> = repository.isActive
}
