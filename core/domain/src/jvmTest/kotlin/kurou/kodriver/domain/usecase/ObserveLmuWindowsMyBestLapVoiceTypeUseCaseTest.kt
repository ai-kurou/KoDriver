package kurou.kodriver.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsMyBestLapVoiceTypeUseCaseTest {

    @Test
    fun `保存済みのLMU自己ベストラップ音声タイプを返す`() = runBlocking {
        val repository = mockk<LmuWindowsMyBestLapPreferencesRepository>()
        every { repository.observeVoiceType() } returns MutableStateFlow(MyBestLapVoiceType.CASUAL)
        val useCase = ObserveLmuWindowsMyBestLapVoiceTypeUseCase(repository)

        assertEquals(MyBestLapVoiceType.CASUAL, useCase().first())
    }
}
