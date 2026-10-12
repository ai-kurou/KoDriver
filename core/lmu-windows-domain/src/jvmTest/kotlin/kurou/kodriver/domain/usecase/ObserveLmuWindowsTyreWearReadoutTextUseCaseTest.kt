package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsTyreWearPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsTyreWearReadoutTextUseCaseTest {
    private val repository: LmuWindowsTyreWearPreferencesRepository = mockk()

    @Test
    fun `タイヤ摩耗読み上げ文言を監視できる`() =
        runTest {
            every { repository.observeReadoutText() } returns MutableStateFlow("残量{percent}%")
            val useCase = ObserveLmuWindowsTyreWearReadoutTextUseCase(repository)

            assertEquals("残量{percent}%", useCase().first())
            verify(exactly = 1) { repository.observeReadoutText() }
            confirmVerified(repository)
        }
}
