package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveReadoutEnabledStateUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: ReadoutPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runBlocking {
            val useCase = SaveReadoutEnabledStateUseCase(repository)

            useCase("lmu_windows", ReadoutItemKey.LmuWindows.MyBestLap.Root, true)
            useCase("lmu_windows", ReadoutItemKey.LmuWindows.MyBestLap.Root, false)

            coVerify(exactly = 1) {
                repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.LmuWindows.MyBestLap.Root, true)
            }
            coVerify(exactly = 1) {
                repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.LmuWindows.MyBestLap.Root, false)
            }
            confirmVerified(repository)
        }
}
