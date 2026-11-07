@file:Suppress("FunctionNaming")

package kurou.kodriver.core.speechrecognitiondata.speech

import kurou.kodriver.domain.repository.RecordAudioPermissionRepository

internal class FakeRecordAudioPermissionRepository(
    var granted: Boolean = true,
) : RecordAudioPermissionRepository {
    override fun isGranted(): Boolean = granted
}
