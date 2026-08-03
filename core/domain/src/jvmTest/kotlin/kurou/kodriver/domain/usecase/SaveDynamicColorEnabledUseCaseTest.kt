package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveDynamicColorEnabledUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: DynamicColorEnabledRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `Repositoryへ保存する`() =
        runTest {
            SaveDynamicColorEnabledUseCase(repository)(true)

            coVerify(exactly = 1) { repository.saveDynamicColorEnabled(true) }
            confirmVerified(repository)
        }
}
