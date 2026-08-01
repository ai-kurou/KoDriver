package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AceWindowsFlagPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveAceWindowsFlagEnabledStateUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: AceWindowsFlagPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `指定したフラグの有効状態が保存される`() =
        runBlocking {
            SaveAceWindowsFlagEnabledStateUseCase(repository)(ReadoutItemKey.AceWindows.Flag.RedFlag, false)

            coVerify(exactly = 1) {
                repository.saveFlagEnabledState(ReadoutItemKey.AceWindows.Flag.RedFlag, false)
            }
            confirmVerified(repository)
        }
}
