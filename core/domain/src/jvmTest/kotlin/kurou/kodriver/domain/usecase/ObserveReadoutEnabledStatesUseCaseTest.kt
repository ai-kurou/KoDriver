package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveReadoutEnabledStatesUseCaseTest {

    @Test
    fun `初期値はデフォルト定義のない未知のシミュレーターでは空Mapを返す`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val useCase = ObserveReadoutEnabledStatesUseCase(repo)

        assertTrue(useCase("rFactor 2").first().isEmpty())
    }

    @Test
    fun `lmu_windowsは保存済みの値がなくてもデフォルト値が反映される`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val useCase = ObserveReadoutEnabledStatesUseCase(repo)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.LmuWindows.Flag.Root to true,
                ReadoutItemKey.LmuWindows.VehicleApproach to true,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root to true,
                ReadoutItemKey.LmuWindows.TyreTemperature.Root to false,
                ReadoutItemKey.LmuWindows.MyBestLap to false,
            ),
            useCase("lmu_windows").first(),
        )
    }

    @Test
    fun `gt7_ps5は保存済みの値がなくてもデフォルトのtrueが反映される`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val useCase = ObserveReadoutEnabledStatesUseCase(repo)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.Gt7Ps5.RemainingFuelLaps to true,
                ReadoutItemKey.Gt7Ps5.MyBestLap to true,
            ),
            useCase("gt7_ps5").first(),
        )
    }

    @Test
    fun `保存済みの値はデフォルトより優先され・シミュレーターごとに独立している`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val useCase = ObserveReadoutEnabledStatesUseCase(repo)

        repo.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.LmuWindows.MyBestLap, true)
        repo.saveReadoutEnabledState("rFactor 2", ReadoutItemKey.LmuWindows.VehicleApproach, false)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.LmuWindows.Flag.Root to true,
                ReadoutItemKey.LmuWindows.VehicleApproach to true,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root to true,
                ReadoutItemKey.LmuWindows.TyreTemperature.Root to false,
                ReadoutItemKey.LmuWindows.MyBestLap to true,
            ),
            useCase("lmu_windows").first(),
        )
        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.VehicleApproach to false),
            useCase("rFactor 2").first(),
        )
    }
}
