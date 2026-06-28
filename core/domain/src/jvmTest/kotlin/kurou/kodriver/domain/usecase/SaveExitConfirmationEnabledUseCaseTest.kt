package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse

class SaveExitConfirmationEnabledUseCaseTest {

    @Test
    fun `終了確認の有効状態を保存できる`() = runBlocking {
        val repository = FakeExitConfirmationPreferencesRepository(initial = true)
        val useCase = SaveExitConfirmationEnabledUseCase(repository)

        useCase(false)

        assertFalse(repository.exitConfirmationEnabled().first())
    }
}
