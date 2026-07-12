package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapEnabledRepository
import kotlin.test.Test
import kotlin.test.assertTrue

private fun createLmuWindowsMyBestLapEnabledRepository(
    initial: Boolean? = null,
): LmuWindowsMyBestLapEnabledRepository {
    val repository = mockk<LmuWindowsMyBestLapEnabledRepository>()
    val state = MutableStateFlow(initial)
    every { repository.observeEnabled() } returns state
    coEvery { repository.saveEnabled(any()) } answers {
        state.update { firstArg() }
    }
    return repository
}

class SaveLmuWindowsMyBestLapEnabledUseCaseTest {

    @Test
    fun `有効状態を保存できる`() = runBlocking {
        val repository = createLmuWindowsMyBestLapEnabledRepository()
        val saveUseCase = SaveLmuWindowsMyBestLapEnabledUseCase(repository)
        val observeUseCase = ObserveLmuWindowsMyBestLapEnabledUseCase(repository)

        saveUseCase(true)

        assertTrue(observeUseCase().first())
    }
}
