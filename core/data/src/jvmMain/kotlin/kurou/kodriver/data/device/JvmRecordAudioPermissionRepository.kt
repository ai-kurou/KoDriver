package kurou.kodriver.data.device

import kurou.kodriver.domain.repository.RecordAudioPermissionRepository

internal class JvmRecordAudioPermissionRepository : RecordAudioPermissionRepository {
    override fun isGranted(): Boolean = true
}
