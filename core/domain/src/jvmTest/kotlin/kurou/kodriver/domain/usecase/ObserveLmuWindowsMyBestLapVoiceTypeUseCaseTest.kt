package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsMyBestLapVoiceTypeUseCaseTest {
    @MockK
    private lateinit var repository: LmuWindowsMyBestLapPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `保存済みのLMU自己ベストラップ音声タイプを返す`() =
        runBlocking {
            every { repository.observeVoiceType() } returns MutableStateFlow(MyBestLapVoiceType.CASUAL)
            val useCase = ObserveLmuWindowsMyBestLapVoiceTypeUseCase(repository)

            assertEquals(MyBestLapVoiceType.CASUAL, useCase().first())
            verify(exactly = 1) { repository.observeVoiceType() }
            confirmVerified(repository)
        }
}
