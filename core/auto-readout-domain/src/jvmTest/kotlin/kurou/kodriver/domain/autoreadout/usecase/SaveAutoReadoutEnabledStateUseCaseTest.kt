package kurou.kodriver.domain.autoreadout.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.autoreadout.repository.AutoReadoutPreferencesRepository
import kotlin.test.Test

class SaveAutoReadoutEnabledStateUseCaseTest {
    private val repository: AutoReadoutPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runTest {
            val useCase = SaveAutoReadoutEnabledStateUseCase(repository)

            useCase("lmu_windows", TestAutoReadoutItemKey.MyBestLap, true)
            useCase("lmu_windows", TestAutoReadoutItemKey.MyBestLap, false)

            coVerify(exactly = 1) {
                repository.saveAutoReadoutEnabledState("lmu_windows", TestAutoReadoutItemKey.MyBestLap, true)
            }
            coVerify(exactly = 1) {
                repository.saveAutoReadoutEnabledState("lmu_windows", TestAutoReadoutItemKey.MyBestLap, false)
            }
            confirmVerified(repository)
        }
}
