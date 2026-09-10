package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsRainPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveLmuWindowsRainEnabledStateUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: LmuWindowsRainPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `指定した降雨項目の有効状態が保存される`() =
        runTest {
            SaveLmuWindowsRainEnabledStateUseCase(repository)(ReadoutItemKey.LmuWindows.Rain.Start, false)

            coVerify(exactly = 1) {
                repository.saveRainEnabledState(ReadoutItemKey.LmuWindows.Rain.Start, false)
            }
            confirmVerified(repository)
        }
}
