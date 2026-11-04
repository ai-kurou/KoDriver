package kurou.kodriver.data.device

import kotlin.test.Test
import kotlin.test.assertTrue

class JvmRecordAudioPermissionRepositoryTest {
    private val repository = JvmRecordAudioPermissionRepository()

    @Test
    fun `isGrantedはtrueを返す`() {
        assertTrue(repository.isGranted())
    }
}
