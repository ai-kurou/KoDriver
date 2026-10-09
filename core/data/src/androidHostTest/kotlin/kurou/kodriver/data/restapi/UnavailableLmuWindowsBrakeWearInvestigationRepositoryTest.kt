@file:Suppress("FunctionNaming")

package kurou.kodriver.data.restapi

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsBrakeWearInvestigationData
import kotlin.test.Test
import kotlin.test.assertEquals

class UnavailableLmuWindowsBrakeWearInvestigationRepositoryTest {
    @Test
    fun `取得できなかった状態の1件だけを流す`() =
        runTest {
            val repository = UnavailableLmuWindowsBrakeWearInvestigationRepository()

            val results = repository.investigationStream().toList()

            assertEquals(listOf(LmuWindowsBrakeWearInvestigationData()), results)
        }
}
