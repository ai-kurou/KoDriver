@file:Suppress("FunctionNaming")

package kurou.kodriver.data

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class EmptyTyreCarcassTemperatureRepositoryTest {

    private val repository = EmptyTyreCarcassTemperatureRepository()

    @Test
    fun `tyreCarcassTemperatureStream は要素を emit せずに完了する`() = runTest {
        val items = repository.tyreCarcassTemperatureStream().toList()
        assertTrue(items.isEmpty())
    }
}
