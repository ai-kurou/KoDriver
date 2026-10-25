package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsPitTimingFuelImminentReadoutTextUseCaseTest {
    private val repository: LmuWindowsPitTimingReadoutTextPreferencesRepository = mockk()

    @Test
    fun `燃料読み上げ文言を監視できる`() =
        runTest {
            every { repository.observeFuelImminentReadoutText() } returns MutableStateFlow("ピットイン")
            val useCase = ObserveLmuWindowsPitTimingFuelImminentReadoutTextUseCase(repository)

            assertEquals("ピットイン", useCase().first())
            verify(exactly = 1) { repository.observeFuelImminentReadoutText() }
            confirmVerified(repository)
        }
}
