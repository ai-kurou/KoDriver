package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createGt7Ps5MyBestLapPreferencesRepository(
    initial: MyBestLapVoiceType = MyBestLapVoiceType.FORMAL,
): Gt7Ps5MyBestLapPreferencesRepository {
    val repository = mockk<Gt7Ps5MyBestLapPreferencesRepository>()
    val state = MutableStateFlow(initial)
    every { repository.observeVoiceType() } returns state
    coEvery { repository.saveVoiceType(any()) } answers {
        state.update { firstArg() }
    }
    return repository
}

class SaveGt7Ps5MyBestLapVoiceTypeUseCaseTest {

    @Test
    fun `音声タイプを保存できる`() = runBlocking {
        val repository = createGt7Ps5MyBestLapPreferencesRepository()
        val saveUseCase = SaveGt7Ps5MyBestLapVoiceTypeUseCase(repository)
        val observeUseCase = ObserveGt7Ps5MyBestLapVoiceTypeUseCase(repository)

        saveUseCase(MyBestLapVoiceType.CASUAL)

        assertEquals(MyBestLapVoiceType.CASUAL, observeUseCase().first())
    }
}
