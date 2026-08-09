package kurou.kodriver.domain.usecase

import kurou.kodriver.core.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolveReadoutOrderUseCaseTest {
    private val useCase = ResolveReadoutOrderUseCase()

    private val flag = ReadoutItemKey.LmuWindows.Flag.Root
    private val myBestLap = ReadoutItemKey.LmuWindows.MyBestLap.Root
    private val vehicleApproach = ReadoutItemKey.LmuWindows.VehicleApproach.Root
    private val vehicleDamage = ReadoutItemKey.LmuWindows.VehicleDamage.Root

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

    @Test
    fun `保存済み順序の全項目が削除済みの場合はデフォルト順序のみ返す`() {
        val persisted = listOf(vehicleDamage)
        val default = listOf(flag, myBestLap)

        assertEquals(listOf(flag, myBestLap), useCase(persistedOrder = persisted, defaultOrder = default))
    }
}
