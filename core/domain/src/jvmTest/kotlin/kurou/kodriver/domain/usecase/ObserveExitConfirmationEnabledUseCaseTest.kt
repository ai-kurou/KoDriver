package kurou.kodriver.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.ExitConfirmationEnabledRepository
import kotlin.test.Test
import kotlin.test.assertTrue

class ObserveExitConfirmationEnabledUseCaseTest {

    @Test
    fun `終了確認の有効状態を監視できる`() = runBlocking {
        val repository = mockk<ExitConfirmationEnabledRepository>()
        every { repository.exitConfirmationEnabled() } returns MutableStateFlow(true)
        val useCase = ObserveExitConfirmationEnabledUseCase(repository)

        assertTrue(useCase().first())
    }
}
