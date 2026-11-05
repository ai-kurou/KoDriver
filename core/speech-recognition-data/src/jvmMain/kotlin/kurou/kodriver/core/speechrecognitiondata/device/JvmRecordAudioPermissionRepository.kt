package kurou.kodriver.core.speechrecognitiondata.device

import kurou.kodriver.domain.repository.RecordAudioPermissionRepository

internal class JvmRecordAudioPermissionRepository : RecordAudioPermissionRepository {
    override fun isGranted(): Boolean = true
}
