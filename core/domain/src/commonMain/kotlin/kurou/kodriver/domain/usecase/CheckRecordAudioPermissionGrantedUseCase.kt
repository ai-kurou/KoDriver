package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.RecordAudioPermissionRepository

class CheckRecordAudioPermissionGrantedUseCase(
    private val repository: RecordAudioPermissionRepository,
) {
    operator fun invoke(): Boolean = repository.isGranted()
}
