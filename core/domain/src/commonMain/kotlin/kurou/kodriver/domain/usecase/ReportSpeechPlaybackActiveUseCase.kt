package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.SpeechPlaybackStateRepository

public class ReportSpeechPlaybackActiveUseCase(
    private val repository: SpeechPlaybackStateRepository,
) {
    public operator fun invoke(
        sourceId: String,
        active: Boolean,
    ): Unit = repository.update(sourceId, active)
}
