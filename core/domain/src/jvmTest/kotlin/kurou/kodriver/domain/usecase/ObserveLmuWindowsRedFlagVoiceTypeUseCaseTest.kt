package kurou.kodriver.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.RedFlagVoiceType
import kurou.kodriver.domain.repository.LmuWindowsRedFlagPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsRedFlagVoiceTypeUseCaseTest {

    @Test
    fun `保存済みの赤旗音声タイプを返す`() = runBlocking {
        val repository = mockk<LmuWindowsRedFlagPreferencesRepository>()
        every { repository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.RED_FLAG)
        val useCase = ObserveLmuWindowsRedFlagVoiceTypeUseCase(repository)

        assertEquals(RedFlagVoiceType.RED_FLAG, useCase().first())
    }
}
