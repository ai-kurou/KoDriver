package kurou.kodriver.domain.autoreadout.usecase

import kotlin.test.Test
import kotlin.test.assertEquals

class ResolveAutoReadoutOrderUseCaseTest {
    private val useCase = ResolveAutoReadoutOrderUseCase()

    private val flag = TestAutoReadoutItemKey.Flag
    private val myBestLap = TestAutoReadoutItemKey.MyBestLap
    private val vehicleApproach = TestAutoReadoutItemKey.VehicleApproach
    private val vehicleDamage = TestAutoReadoutItemKey.VehicleDamage

    @Test
    fun `保存済み順序が空の場合はデフォルト順序を返す`() {
        val default = listOf(flag, myBestLap, vehicleApproach)

        assertEquals(default, useCase(persistedOrder = emptyList(), defaultOrder = default))
    }

    @Test
    fun `保存済み順序を維持しつつ削除済み項目を除外し新規項目を末尾に補完する`() {
        val persisted = listOf(vehicleApproach, vehicleDamage, flag)
        val default = listOf(flag, myBestLap, vehicleApproach)

        val result = useCase(persistedOrder = persisted, defaultOrder = default)

        assertEquals(listOf(vehicleApproach, flag, myBestLap), result)
    }
}
