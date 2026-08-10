package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveKeepScreenOnEnabledUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: KeepScreenOnEnabledRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `Repositoryへ保存する`() =
        runTest {
            SaveKeepScreenOnEnabledUseCase(repository)(true)

            coVerify(exactly = 1) { repository.saveKeepScreenOn(true) }
            confirmVerified(repository)
        }
}
