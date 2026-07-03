@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveLmuWindowsVehicleDamageEnabledStateUseCaseTest {

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() = runBlocking {
        val repo = FakeLmuWindowsVehicleDamagePreferencesRepository()
        val saveUseCase = SaveLmuWindowsVehicleDamageEnabledStateUseCase(repo)
        val observeUseCase = ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(repo)

        saveUseCase(ReadoutItemKey.Overheat, true)
        assertEquals(mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.Overheat to true), observeUseCase().first())

        saveUseCase(ReadoutItemKey.Overheat, false)
        assertEquals(mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.Overheat to false), observeUseCase().first())
    }
}
