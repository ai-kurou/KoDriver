package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.ExitConfirmationEnabledRepository
import kotlin.test.Test
import kotlin.test.assertFalse

private fun createExitConfirmationEnabledRepository(initial: Boolean): ExitConfirmationEnabledRepository {
    val repository = mockk<ExitConfirmationEnabledRepository>()
    val state = MutableStateFlow(initial)
    every { repository.exitConfirmationEnabled() } returns state
    coEvery { repository.saveExitConfirmationEnabled(any()) } answers {
        state.update { firstArg() }
    }
    return repository
}

class SaveExitConfirmationEnabledUseCaseTest {

    @Test
    fun `終了確認の有効状態を保存できる`() = runBlocking {
        val repository = createExitConfirmationEnabledRepository(initial = true)
        val useCase = SaveExitConfirmationEnabledUseCase(repository)

        useCase(false)

        assertFalse(repository.exitConfirmationEnabled().first())
    }
}
