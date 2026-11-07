package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.SpeechPlaybackStateRepository

class ReportSpeechPlaybackActiveUseCase(
    private val repository: SpeechPlaybackStateRepository,
) {
    operator fun invoke(
        sourceId: String,
        active: Boolean,
    ) = repository.update(sourceId, active)
}
