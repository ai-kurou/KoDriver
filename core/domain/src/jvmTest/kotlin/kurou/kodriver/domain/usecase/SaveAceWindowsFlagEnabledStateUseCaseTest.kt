package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AceWindowsFlagPreferencesRepository
import kotlin.test.Test

class SaveAceWindowsFlagEnabledStateUseCaseTest {
    private val repository: AceWindowsFlagPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `指定したフラグの有効状態が保存される`() =
        runTest {
            SaveAceWindowsFlagEnabledStateUseCase(repository)(ReadoutItemKey.AceWindows.Flag.RedFlag, false)

            coVerify(exactly = 1) {
                repository.saveFlagEnabledState(ReadoutItemKey.AceWindows.Flag.RedFlag, false)
            }
            confirmVerified(repository)
        }
}
