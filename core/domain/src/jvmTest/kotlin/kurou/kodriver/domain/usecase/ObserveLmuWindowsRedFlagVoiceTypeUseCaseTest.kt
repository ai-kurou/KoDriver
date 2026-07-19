package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.RedFlagVoiceType
import kurou.kodriver.domain.repository.LmuWindowsRedFlagPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsRedFlagVoiceTypeUseCaseTest {

    @MockK
    private lateinit var repository: LmuWindowsRedFlagPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `保存済みの赤旗音声タイプを返す`() = runBlocking {
        every { repository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.RED_FLAG)
        val useCase = ObserveLmuWindowsRedFlagVoiceTypeUseCase(repository)

        assertEquals(RedFlagVoiceType.RED_FLAG, useCase().first())
        verify(exactly = 1) { repository.observeVoiceType() }
        confirmVerified(repository)
    }
}
