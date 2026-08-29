package kurou.kodriver.data.device

import kotlin.test.Test
import kotlin.test.assertTrue

class JvmAccessLocalNetworkPermissionRepositoryTest {
    private val repository = JvmAccessLocalNetworkPermissionRepository()

    @Test
    fun `isGrantedはtrueを返す`() {
        assertTrue(repository.isGranted())
    }
}
