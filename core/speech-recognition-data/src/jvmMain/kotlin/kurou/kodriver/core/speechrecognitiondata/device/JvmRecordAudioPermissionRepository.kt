package kurou.kodriver.core.speechrecognitiondata.device

import kurou.kodriver.domain.microphone.repository.RecordAudioPermissionRepository

internal class JvmRecordAudioPermissionRepository : RecordAudioPermissionRepository {
    override fun isGranted(): Boolean = true
}
