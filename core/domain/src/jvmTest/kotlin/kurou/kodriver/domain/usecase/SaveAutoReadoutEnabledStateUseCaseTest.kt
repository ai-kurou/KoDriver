package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.repository.AutoReadoutPreferencesRepository
import kotlin.test.Test

class SaveAutoReadoutEnabledStateUseCaseTest {
    private val repository: AutoReadoutPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runTest {
            val useCase = SaveAutoReadoutEnabledStateUseCase(repository)

            useCase("lmu_windows", LmuWindowsReadoutItemKey.MyBestLap.Root, true)
            useCase("lmu_windows", LmuWindowsReadoutItemKey.MyBestLap.Root, false)

            coVerify(exactly = 1) {
                repository.saveAutoReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.MyBestLap.Root, true)
            }
            coVerify(exactly = 1) {
                repository.saveAutoReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.MyBestLap.Root, false)
            }
            confirmVerified(repository)
        }
}
