@file:Suppress("FunctionNaming")

package kurou.kodriver.data

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmptyLmuRepositoryTest {

    private val repository = EmptyLmuRepository()

    @Test
    fun `telemetryStream は要素を emit せずに完了する`() = runTest {
        val items = repository.telemetryStream().toList()
        assertTrue(items.isEmpty())
    }

    @Test
    fun `isConnected は false を返す`() = runTest {
        assertFalse(repository.isConnected())
    }

    @Test
    fun `disconnect は例外なく完了する`() = runTest {
        repository.disconnect()
    }
}
