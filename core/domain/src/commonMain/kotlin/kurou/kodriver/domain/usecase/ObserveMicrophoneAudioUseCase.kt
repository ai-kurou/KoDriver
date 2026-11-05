package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.model.MicrophoneAudioEvent
import kurou.kodriver.domain.repository.MicrophoneRepository

class ObserveMicrophoneAudioUseCase(
    private val repository: MicrophoneRepository,
) {
    operator fun invoke(device: AudioInputDevice? = null): Flow<MicrophoneAudioEvent> = repository.observeAudio(device)
}
