package kurou.kodriver.domain.microphone.usecase

import kurou.kodriver.domain.microphone.repository.RecordAudioPermissionRepository

public class CheckRecordAudioPermissionGrantedUseCase(
    private val repository: RecordAudioPermissionRepository,
) {
    public operator fun invoke(): Boolean = repository.isGranted()
}
