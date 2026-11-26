package kurou.kodriver.domain.autoreadout.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.autoreadout.repository.AutoReadoutPreferencesRepository
import kotlin.test.Test

class SaveAutoReadoutOrderUseCaseTest {
    private val repository: AutoReadoutPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runTest {
            val useCase = SaveAutoReadoutOrderUseCase(repository)
            val firstOrder =
                listOf(
                    TestAutoReadoutItemKey.VehicleApproach,
                    TestAutoReadoutItemKey.Flag,
                    TestAutoReadoutItemKey.VehicleDamage,
                )
            val secondOrder =
                listOf(
                    TestAutoReadoutItemKey.Flag,
                    TestAutoReadoutItemKey.VehicleDamage,
                    TestAutoReadoutItemKey.VehicleApproach,
                )

            useCase("lmu_windows", firstOrder)
            useCase("lmu_windows", secondOrder)

            coVerify(exactly = 1) { repository.saveAutoReadoutOrder("lmu_windows", firstOrder) }
            coVerify(exactly = 1) { repository.saveAutoReadoutOrder("lmu_windows", secondOrder) }
            confirmVerified(repository)
        }
}
