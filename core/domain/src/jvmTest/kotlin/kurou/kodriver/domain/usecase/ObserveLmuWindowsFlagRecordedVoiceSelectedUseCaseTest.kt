package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsFlagReadoutTarget
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertTrue

class ObserveLmuWindowsFlagRecordedVoiceSelectedUseCaseTest {
    private val repository: LmuWindowsFlagReadoutTextPreferencesRepository = mockk()

    @Test
    fun `指定したフラッグの収録音声選択状態を返す`() =
        runTest {
            every { repository.observeRecordedVoiceSelected(LmuWindowsFlagReadoutTarget.RED_FLAG) } returns flowOf(true)

            val useCase = ObserveLmuWindowsFlagRecordedVoiceSelectedUseCase(repository)

            val result = useCase(LmuWindowsFlagReadoutTarget.RED_FLAG)

            assertTrue(result.first())
            verify(exactly = 1) { repository.observeRecordedVoiceSelected(LmuWindowsFlagReadoutTarget.RED_FLAG) }
            confirmVerified(repository)
        }
}
