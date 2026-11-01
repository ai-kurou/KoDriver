package kurou.kodriver.domain.gt7ps5.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.registerGt7Ps5ReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5PreferencesUseCasesTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerGt7Ps5ReadoutItemKeys()
    }

    @Test
    fun `燃料残り周回数を監視するUseCaseはリポジトリの監視Flowを返す`() =
        runTest {
            val repository: Gt7Ps5RemainingFuelLapsPreferencesRepository = mockk()
            every { repository.observeRemainingFuelLaps() } returns flowOf(5)

            assertEquals(5, ObserveGt7Ps5RemainingFuelLapsUseCase(repository)().first())
            verify(exactly = 1) { repository.observeRemainingFuelLaps() }
            confirmVerified(repository)
        }

    @Test
    fun `燃料残り周回数を保存するUseCaseはリポジトリの保存メソッドへ委譲する`() =
        runTest {
            val repository: Gt7Ps5RemainingFuelLapsPreferencesRepository = mockk(relaxUnitFun = true)

            SaveGt7Ps5RemainingFuelLapsUseCase(repository)(1)

            coVerify(exactly = 1) { repository.saveRemainingFuelLaps(1) }
            confirmVerified(repository)
        }

    @Test
    fun `タイヤ温度の有効状態を保存するUseCaseはリポジトリの保存メソッドへ委譲する`() =
        runTest {
            val repository: Gt7Ps5TyreTemperaturePreferencesRepository = mockk(relaxUnitFun = true)

            SaveGt7Ps5TyreTemperatureEnabledStateUseCase(repository)(
                Gt7Ps5ReadoutItemKey.TyreTemperature.OverheatWarning,
                false,
            )

            coVerify(exactly = 1) {
                repository.saveEnabledState(Gt7Ps5ReadoutItemKey.TyreTemperature.OverheatWarning, false)
            }
            confirmVerified(repository)
        }
}
