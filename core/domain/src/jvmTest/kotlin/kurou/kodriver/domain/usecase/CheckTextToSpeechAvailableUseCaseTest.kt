package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckTextToSpeechAvailableUseCaseTest {
    private val repository: TextToSpeechRepository = mockk()

    @Test
    fun `利用可能な場合はtrueを返す`() =
        runTest {
            coEvery { repository.isAvailable() } returns true

            assertTrue(CheckTextToSpeechAvailableUseCase(repository)())
        }

    @Test
    fun `利用できない場合はfalseを返す`() =
        runTest {
            coEvery { repository.isAvailable() } returns false

            assertFalse(CheckTextToSpeechAvailableUseCase(repository)())
        }
}
