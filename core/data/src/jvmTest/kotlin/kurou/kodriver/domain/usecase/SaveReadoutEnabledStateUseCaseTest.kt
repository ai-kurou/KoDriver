package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kotlin.test.Test

class SaveReadoutEnabledStateUseCaseTest {
    private val repository: ReadoutPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runTest {
            val useCase = SaveReadoutEnabledStateUseCase(repository)

            useCase("lmu_windows", LmuWindowsReadoutItemKey.MyBestLap.Root, true)
            useCase("lmu_windows", LmuWindowsReadoutItemKey.MyBestLap.Root, false)

            coVerify(exactly = 1) {
                repository.saveReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.MyBestLap.Root, true)
            }
            coVerify(exactly = 1) {
                repository.saveReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.MyBestLap.Root, false)
            }
            confirmVerified(repository)
        }
}
