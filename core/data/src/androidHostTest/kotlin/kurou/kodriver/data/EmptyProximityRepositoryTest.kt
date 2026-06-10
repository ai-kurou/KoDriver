@file:Suppress("FunctionNaming")

package kurou.kodriver.data

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class EmptyProximityRepositoryTest {

    private val repository = EmptyProximityRepository()

    @Test
    fun `proximityStream は要素を emit せずに完了する`() = runTest {
        val items = repository.proximityStream().toList()
        assertTrue(items.isEmpty())
    }
}
