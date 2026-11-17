package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.RecordAudioPermissionRepository

public class CheckRecordAudioPermissionGrantedUseCase(
    private val repository: RecordAudioPermissionRepository,
) {
    public operator fun invoke(): Boolean = repository.isGranted()
}
