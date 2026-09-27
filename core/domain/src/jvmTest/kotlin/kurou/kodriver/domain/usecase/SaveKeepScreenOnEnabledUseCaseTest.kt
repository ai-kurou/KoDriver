package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kotlin.test.Test

class SaveKeepScreenOnEnabledUseCaseTest {
    private val repository: KeepScreenOnEnabledRepository = mockk(relaxUnitFun = true)

    @Test
    fun `Repositoryへ保存する`() =
        runTest {
            SaveKeepScreenOnEnabledUseCase(repository)(true)

            coVerify(exactly = 1) { repository.saveKeepScreenOn(true) }
            confirmVerified(repository)
        }
}
