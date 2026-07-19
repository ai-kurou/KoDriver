package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsFlagEnabledStateUseCaseTest {

    @Test
    fun `指定したフラグの有効状態が保存される`() = runBlocking {
        val repository = mockk<LmuWindowsFlagPreferencesRepository>(relaxUnitFun = true)

        SaveLmuWindowsFlagEnabledStateUseCase(repository)(ReadoutItemKey.LmuWindows.Flag.RedFlag, false)

        coVerify(exactly = 1) {
            repository.saveFlagEnabledState(ReadoutItemKey.LmuWindows.Flag.RedFlag, false)
        }
        confirmVerified(repository)
    }
}
