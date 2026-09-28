package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.TextToSpeechUnavailableReason
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CheckTextToSpeechUnavailableReasonUseCaseTest {
    private val repository: TextToSpeechRepository = mockk()

    @Test
    fun `利用可能な場合はnullを返す`() =
        runTest {
            coEvery { repository.unavailableReason() } returns null

            assertNull(CheckTextToSpeechUnavailableReasonUseCase(repository)())
        }

    @Test
    fun `エンジン未インストールの場合はEngineMissingを返す`() =
        runTest {
            coEvery { repository.unavailableReason() } returns TextToSpeechUnavailableReason.EngineMissing

            assertEquals(
                TextToSpeechUnavailableReason.EngineMissing,
                CheckTextToSpeechUnavailableReasonUseCase(repository)(),
            )
        }

    @Test
    fun `言語データ未インストールの場合はLanguageDataMissingを返す`() =
        runTest {
            coEvery { repository.unavailableReason() } returns TextToSpeechUnavailableReason.LanguageDataMissing

            assertEquals(
                TextToSpeechUnavailableReason.LanguageDataMissing,
                CheckTextToSpeechUnavailableReasonUseCase(repository)(),
            )
        }
}
