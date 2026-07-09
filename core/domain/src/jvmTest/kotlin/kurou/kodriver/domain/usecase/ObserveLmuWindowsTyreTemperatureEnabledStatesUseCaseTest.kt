@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsTyreTemperatureEnabledStatesUseCaseTest {

    @Test
    fun `初期値はOverheatWarningとLowWarningのデフォルトtrueを返す`() = runBlocking {
        val repo = FakeLmuWindowsTyreTemperaturePreferencesRepository()
        val useCase = ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase(repo)

        val expected = mapOf<ReadoutItemKey, Boolean>(
            ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to true,
            ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning to true,
        )
        assertEquals(expected, useCase().first())
    }

    @Test
    fun `保存済みの値はデフォルトより優先される`() = runBlocking {
        val repo = FakeLmuWindowsTyreTemperaturePreferencesRepository()
        val useCase = ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase(repo)

        repo.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, false)

        val expected = mapOf<ReadoutItemKey, Boolean>(
            ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to false,
            ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning to true,
        )
        assertEquals(expected, useCase().first())
    }

    @Test
    fun `デフォルトにないキーを保存した場合そのエントリも返す`() = runBlocking {
        val repo = FakeLmuWindowsTyreTemperaturePreferencesRepository()
        val useCase = ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase(repo)

        repo.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.Root, false)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to true,
                ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning to true,
                ReadoutItemKey.LmuWindows.TyreTemperature.Root to false,
            ),
            useCase().first(),
        )
    }
}
