package kurou.kodriver.data

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class EmptyFlagRepositoryTest {

    private val repository = EmptyFlagRepository()

    @Test
    fun `flagStream は要素を emit せずに完了する`() = runTest {
        val items = repository.flagStream().toList()
        assertTrue(items.isEmpty())
    }
}
