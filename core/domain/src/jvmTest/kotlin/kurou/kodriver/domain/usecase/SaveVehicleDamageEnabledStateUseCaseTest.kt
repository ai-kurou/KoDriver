@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveVehicleDamageEnabledStateUseCaseTest {

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() = runBlocking {
        val repo = FakeVehicleDamagePreferencesRepository()
        val saveUseCase = SaveVehicleDamageEnabledStateUseCase(repo)
        val observeUseCase = ObserveVehicleDamageEnabledStatesUseCase(repo)

        saveUseCase("overheat", true)
        assertEquals(mapOf("overheat" to true), observeUseCase().first())

        saveUseCase("overheat", false)
        assertEquals(mapOf("overheat" to false), observeUseCase().first())
    }
}
