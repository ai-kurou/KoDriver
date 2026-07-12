package kurou.kodriver.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveGt7Ps5MyBestLapVoiceTypeUseCaseTest {

    @Test
    fun `音声タイプを監視できる`() = runBlocking {
        val repository = mockk<Gt7Ps5MyBestLapPreferencesRepository>()
        every { repository.observeVoiceType() } returns MutableStateFlow(MyBestLapVoiceType.CASUAL)
        val useCase = ObserveGt7Ps5MyBestLapVoiceTypeUseCase(repository)

        assertEquals(MyBestLapVoiceType.CASUAL, useCase().first())
    }
}
