package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCaseTest {
    private val repository: LmuWindowsPitTimingReadoutTextPreferencesRepository = mockk()

    @Test
    fun `タイヤ摩耗読み上げ文言を監視できる`() =
        runTest {
            every { repository.observeTyreWearImminentReadoutText() } returns MutableStateFlow("ピットイン")
            val useCase = ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase(repository)

            assertEquals("ピットイン", useCase().first())
            verify(exactly = 1) { repository.observeTyreWearImminentReadoutText() }
            confirmVerified(repository)
        }
}
