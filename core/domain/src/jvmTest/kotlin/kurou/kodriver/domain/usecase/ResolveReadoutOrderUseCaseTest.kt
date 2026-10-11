package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolveReadoutOrderUseCaseTest {
    private val useCase = ResolveReadoutOrderUseCase()

    private val flag = LmuWindowsReadoutItemKey.Flag.Root
    private val myBestLap = LmuWindowsReadoutItemKey.MyBestLap.Root
    private val vehicleApproach = LmuWindowsReadoutItemKey.VehicleApproach.Root
    private val vehicleDamage = LmuWindowsReadoutItemKey.VehicleDamage.Root

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
