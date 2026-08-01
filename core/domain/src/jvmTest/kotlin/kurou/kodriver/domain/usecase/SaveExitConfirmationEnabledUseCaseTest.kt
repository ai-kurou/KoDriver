package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.ExitConfirmationEnabledRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveExitConfirmationEnabledUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: ExitConfirmationEnabledRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `終了確認の有効状態を保存できる`() =
        runBlocking {
            SaveExitConfirmationEnabledUseCase(repository)(false)

            coVerify(exactly = 1) { repository.saveExitConfirmationEnabled(false) }
            confirmVerified(repository)
        }
}
