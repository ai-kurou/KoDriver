package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.ExitConfirmationEnabledRepository
import kotlin.test.Test

class SaveExitConfirmationEnabledUseCaseTest {

    @Test
    fun `終了確認の有効状態を保存できる`() = runBlocking {
        val repository = mockk<ExitConfirmationEnabledRepository>(relaxUnitFun = true)

        SaveExitConfirmationEnabledUseCase(repository)(false)

        coVerify(exactly = 1) { repository.saveExitConfirmationEnabled(false) }
        confirmVerified(repository)
    }
}
