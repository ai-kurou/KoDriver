@file:Suppress("FunctionNaming")

package kurou.kodriver.data.restapi

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UnavailableLmuWindowsBrakeWearRepositoryTest {
    @Test
    fun `何も流さずに完了する`() =
        runTest {
            val repository = UnavailableLmuWindowsBrakeWearRepository()

            assertEquals(emptyList(), repository.brakeWearStream().toList())
        }
}
