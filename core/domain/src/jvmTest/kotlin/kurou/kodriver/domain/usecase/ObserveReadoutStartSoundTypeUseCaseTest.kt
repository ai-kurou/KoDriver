package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveReadoutStartSoundTypeUseCaseTest {
    @MockK
    private lateinit var repository: ReadoutStartSoundPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `読み上げ開始音種別を監視できる`() =
        runBlocking {
            every { repository.observeType() } returns MutableStateFlow(ReadoutStartSoundType.FORMULA_RADIO)
            val useCase = ObserveReadoutStartSoundTypeUseCase(repository)

            assertEquals(ReadoutStartSoundType.FORMULA_RADIO, useCase().first())
            verify(exactly = 1) { repository.observeType() }
            confirmVerified(repository)
        }
}
