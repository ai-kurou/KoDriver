package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kotlin.test.Test

class SaveReadoutOrderUseCaseTest {
    private val repository: ReadoutPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runTest {
            val useCase = SaveReadoutOrderUseCase(repository)
            val firstOrder =
                listOf(
                    LmuWindowsReadoutItemKey.VehicleApproach.Root,
                    LmuWindowsReadoutItemKey.Flag.Root,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root,
                )
            val secondOrder =
                listOf(
                    LmuWindowsReadoutItemKey.Flag.Root,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root,
                    LmuWindowsReadoutItemKey.VehicleApproach.Root,
                )

            useCase("lmu_windows", firstOrder)
            useCase("lmu_windows", secondOrder)

            coVerify(exactly = 1) { repository.saveReadoutOrder("lmu_windows", firstOrder) }
            coVerify(exactly = 1) { repository.saveReadoutOrder("lmu_windows", secondOrder) }
            confirmVerified(repository)
        }
}
