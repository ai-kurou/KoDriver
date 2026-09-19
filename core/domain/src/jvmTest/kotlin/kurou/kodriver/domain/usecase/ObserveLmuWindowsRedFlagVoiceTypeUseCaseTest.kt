package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.RedFlagVoiceType
import kurou.kodriver.domain.repository.LmuWindowsRedFlagPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsRedFlagVoiceTypeUseCaseTest {
    private val repository: LmuWindowsRedFlagPreferencesRepository = mockk()

    @Test
    fun `保存済みの赤旗音声タイプを返す`() =
        runTest {
            every { repository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.RED_FLAG)
            val useCase = ObserveLmuWindowsRedFlagVoiceTypeUseCase(repository)

            assertEquals(RedFlagVoiceType.RED_FLAG, useCase().first())
            verify(exactly = 1) { repository.observeVoiceType() }
            confirmVerified(repository)
        }
}
