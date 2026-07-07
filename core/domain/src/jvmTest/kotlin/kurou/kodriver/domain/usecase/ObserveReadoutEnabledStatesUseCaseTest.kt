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
    fun `lmu_windowsは保存済みの値がなくてもデフォルトのfalseが反映される`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val useCase = ObserveReadoutEnabledStatesUseCase(repo)

        assertEquals(
            mapOf(
                ReadoutItemKey.TyreTemperature to false,
                ReadoutItemKey.MyBestLap to false,
            ),
            useCase("lmu_windows").first(),
        )
    }

    @Test
    fun `保存済みの値はデフォルトより優先され・シミュレーターごとに独立している`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val useCase = ObserveReadoutEnabledStatesUseCase(repo)

        repo.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VehicleApproach, true)
        repo.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.MyBestLap, true)
        repo.saveReadoutEnabledState("rFactor 2", ReadoutItemKey.VehicleApproach, false)

        assertEquals(
            mapOf(
                ReadoutItemKey.TyreTemperature to false,
                ReadoutItemKey.MyBestLap to true,
                ReadoutItemKey.VehicleApproach to true,
            ),
            useCase("lmu_windows").first(),
        )
        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.VehicleApproach to false),
            useCase("rFactor 2").first(),
        )
    }
}
