package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCaseTest {
    private val repository: LmuWindowsTyreTemperaturePreferencesRepository = mockk()

    @Test
    fun `タイヤ過熱警告の読み上げ文言を監視できる`() =
        runTest {
            every { repository.observeOverheatReadoutText() } returns MutableStateFlow("タイヤが過熱しています")
            val useCase = ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase(repository)

            assertEquals("タイヤが過熱しています", useCase().first())
            verify(exactly = 1) { repository.observeOverheatReadoutText() }
            confirmVerified(repository)
        }
}
