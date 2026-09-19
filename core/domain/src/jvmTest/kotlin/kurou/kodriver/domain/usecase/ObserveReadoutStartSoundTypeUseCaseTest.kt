package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveReadoutStartSoundTypeUseCaseTest {
    private val repository: ReadoutStartSoundPreferencesRepository = mockk()

    @Test
    fun `読み上げ開始音種別を監視できる`() =
        runTest {
            every { repository.observeType() } returns MutableStateFlow(ReadoutStartSoundType.FORMULA_RADIO)
            val useCase = ObserveReadoutStartSoundTypeUseCase(repository)

            assertEquals(ReadoutStartSoundType.FORMULA_RADIO, useCase().first())
            verify(exactly = 1) { repository.observeType() }
            confirmVerified(repository)
        }
}
