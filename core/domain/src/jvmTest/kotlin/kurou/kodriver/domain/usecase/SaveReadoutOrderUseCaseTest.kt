package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveReadoutOrderUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: ReadoutPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runTest {
            val useCase = SaveReadoutOrderUseCase(repository)
            val firstOrder =
                listOf(
                    ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    ReadoutItemKey.LmuWindows.Flag.Root,
                    ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                )
            val secondOrder =
                listOf(
                    ReadoutItemKey.LmuWindows.Flag.Root,
                    ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                )

            useCase("lmu_windows", firstOrder)
            useCase("lmu_windows", secondOrder)

            coVerify(exactly = 1) { repository.saveReadoutOrder("lmu_windows", firstOrder) }
            coVerify(exactly = 1) { repository.saveReadoutOrder("lmu_windows", secondOrder) }
            confirmVerified(repository)
        }
}
