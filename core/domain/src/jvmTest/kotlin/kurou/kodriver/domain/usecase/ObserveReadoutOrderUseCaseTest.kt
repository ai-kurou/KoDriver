package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveReadoutOrderUseCaseTest {

    @Test
    fun `初期値は空リスト・保存済みの順序を返す・シミュレーターごとに独立している`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val useCase = ObserveReadoutOrderUseCase(repo)

        assertTrue(useCase("lmu_windows").first().isEmpty())

        repo.saveReadoutOrder(
            "lmu_windows",
            listOf(ReadoutItemKey.VehicleApproach, ReadoutItemKey.Flag, ReadoutItemKey.VehicleDamage),
        )
        repo.saveReadoutOrder("rFactor 2", listOf(ReadoutItemKey.Flag))

        assertEquals(
            listOf(ReadoutItemKey.VehicleApproach, ReadoutItemKey.Flag, ReadoutItemKey.VehicleDamage),
            useCase("lmu_windows").first(),
        )
        assertEquals(listOf(ReadoutItemKey.Flag), useCase("rFactor 2").first())
    }
}
