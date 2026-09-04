package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.OverheatVoiceType
import kurou.kodriver.domain.repository.LmuWindowsOverheatPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsOverheatVoiceTypeUseCaseTest {
    @MockK
    private lateinit var repository: LmuWindowsOverheatPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `保存済みのオーバーヒート音声タイプを返す`() =
        runTest {
            every { repository.observeVoiceType() } returns MutableStateFlow(OverheatVoiceType.STANDARD)
            val useCase = ObserveLmuWindowsOverheatVoiceTypeUseCase(repository)

            assertEquals(OverheatVoiceType.STANDARD, useCase().first())
            verify(exactly = 1) { repository.observeVoiceType() }
            confirmVerified(repository)
        }
}
