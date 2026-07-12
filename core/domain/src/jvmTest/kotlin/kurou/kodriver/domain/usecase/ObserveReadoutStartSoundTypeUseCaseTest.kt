package kurou.kodriver.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveReadoutStartSoundTypeUseCaseTest {

    @Test
    fun `読み上げ開始音種別を監視できる`() = runBlocking {
        val repository = mockk<ReadoutStartSoundPreferencesRepository>()
        every { repository.observeType() } returns MutableStateFlow(ReadoutStartSoundType.FORMULA_RADIO)
        val useCase = ObserveReadoutStartSoundTypeUseCase(repository)

        assertEquals(ReadoutStartSoundType.FORMULA_RADIO, useCase().first())
    }
}
