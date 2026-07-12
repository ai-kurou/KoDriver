package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsMyBestLapPreferencesRepository(
    initial: MyBestLapVoiceType = MyBestLapVoiceType.FORMAL,
): LmuWindowsMyBestLapPreferencesRepository {
    val repository = mockk<LmuWindowsMyBestLapPreferencesRepository>()
    val state = MutableStateFlow(initial)
    every { repository.observeVoiceType() } returns state
    coEvery { repository.saveVoiceType(any()) } answers {
        state.update { firstArg() }
    }
    return repository
}

class SaveLmuWindowsMyBestLapVoiceTypeUseCaseTest {

    @Test
    fun `LMU自己ベストラップ音声タイプを保存する`() = runBlocking {
        val repository = createLmuWindowsMyBestLapPreferencesRepository()
        val saveUseCase = SaveLmuWindowsMyBestLapVoiceTypeUseCase(repository)
        val observeUseCase = ObserveLmuWindowsMyBestLapVoiceTypeUseCase(repository)

        saveUseCase(MyBestLapVoiceType.CASUAL)

        assertEquals(MyBestLapVoiceType.CASUAL, observeUseCase().first())
    }
}
