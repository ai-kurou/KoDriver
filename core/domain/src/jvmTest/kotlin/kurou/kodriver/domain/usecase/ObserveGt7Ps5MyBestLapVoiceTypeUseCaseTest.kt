package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveGt7Ps5MyBestLapVoiceTypeUseCaseTest {
    @MockK
    private lateinit var repository: Gt7Ps5MyBestLapPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `音声タイプを監視できる`() =
        runTest {
            every { repository.observeVoiceType() } returns MutableStateFlow(MyBestLapVoiceType.CASUAL)
            val useCase = ObserveGt7Ps5MyBestLapVoiceTypeUseCase(repository)

            assertEquals(MyBestLapVoiceType.CASUAL, useCase().first())
            verify(exactly = 1) { repository.observeVoiceType() }
            confirmVerified(repository)
        }
}
