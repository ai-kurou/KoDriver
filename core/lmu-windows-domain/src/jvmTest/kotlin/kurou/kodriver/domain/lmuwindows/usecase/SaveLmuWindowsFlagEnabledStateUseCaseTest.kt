package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveLmuWindowsFlagEnabledStateUseCaseTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerLmuWindowsReadoutItemKeys()
    }

    private val repository: LmuWindowsFlagPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `指定したフラッグの有効状態が保存される`() =
        runTest {
            SaveLmuWindowsFlagEnabledStateUseCase(repository)(LmuWindowsReadoutItemKey.Flag.RedFlag, false)

            coVerify(exactly = 1) {
                repository.saveFlagEnabledState(LmuWindowsReadoutItemKey.Flag.RedFlag, false)
            }
            confirmVerified(repository)
        }
}
