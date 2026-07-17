package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.RedFlagVoiceType
import kurou.kodriver.domain.repository.LmuWindowsRedFlagPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsRedFlagPreferencesRepository(
    initial: RedFlagVoiceType = RedFlagVoiceType.SESSION_STOP,
): LmuWindowsRedFlagPreferencesRepository {
    val repository = mockk<LmuWindowsRedFlagPreferencesRepository>()
    val state = MutableStateFlow(initial)
    every { repository.observeVoiceType() } returns state
    coEvery { repository.saveVoiceType(any()) } answers {
        state.update { firstArg() }
    }
    return repository
}

class SaveLmuWindowsRedFlagVoiceTypeUseCaseTest {

    @Test
    fun `赤旗音声タイプを保存する`() = runBlocking {
        val repository = createLmuWindowsRedFlagPreferencesRepository()
        val saveUseCase = SaveLmuWindowsRedFlagVoiceTypeUseCase(repository)
        val observeUseCase = ObserveLmuWindowsRedFlagVoiceTypeUseCase(repository)

        saveUseCase(RedFlagVoiceType.RED_FLAG)

        assertEquals(RedFlagVoiceType.RED_FLAG, observeUseCase().first())
    }
}
