package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class ObserveExitConfirmationEnabledUseCaseTest {

    @Test
    fun `終了確認の有効状態を監視できる`() = runBlocking {
        val repository = FakeExitConfirmationEnabledRepository(initial = true)
        val useCase = ObserveExitConfirmationEnabledUseCase(repository)

        assertTrue(useCase().first())
    }
}
