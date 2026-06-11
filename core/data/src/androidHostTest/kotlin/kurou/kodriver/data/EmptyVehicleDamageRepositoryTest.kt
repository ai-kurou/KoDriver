@file:Suppress("FunctionNaming")

package kurou.kodriver.data

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class EmptyVehicleDamageRepositoryTest {

    private val repository = EmptyVehicleDamageRepository()

    @Test
    fun `vehicleDamageStream は要素を emit せずに完了する`() = runTest {
        val items = repository.vehicleDamageStream().toList()
        assertTrue(items.isEmpty())
    }
}
