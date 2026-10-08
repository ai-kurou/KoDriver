package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperatureReadoutTextPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCaseTest {
    private val repository: LmuWindowsTyreTemperatureReadoutTextPreferencesRepository = mockk()

    @Test
    fun `タイヤ低温警告の読み上げ文言を監視できる`() =
        runTest {
            every { repository.observeColdReadoutText() } returns MutableStateFlow("タイヤが冷えています")
            val useCase = ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase(repository)

            assertEquals("タイヤが冷えています", useCase().first())
            verify(exactly = 1) { repository.observeColdReadoutText() }
            confirmVerified(repository)
        }
}
