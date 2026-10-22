package kurou.kodriver.domain.acewindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.registerAceWindowsReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveAceWindowsFlagEnabledStateUseCaseTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerAceWindowsReadoutItemKeys()
    }

    private val repository: AceWindowsFlagPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `指定したフラッグの有効状態が保存される`() =
        runTest {
            SaveAceWindowsFlagEnabledStateUseCase(repository)(AceWindowsReadoutItemKey.Flag.RedFlag, false)

            coVerify(exactly = 1) {
                repository.saveFlagEnabledState(AceWindowsReadoutItemKey.Flag.RedFlag, false)
            }
            confirmVerified(repository)
        }
}
