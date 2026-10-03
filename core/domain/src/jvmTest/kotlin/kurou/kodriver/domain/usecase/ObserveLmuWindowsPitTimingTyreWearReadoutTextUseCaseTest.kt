package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCaseTest {
    private val repository: LmuWindowsPitTimingPreferencesRepository = mockk()

    @Test
    fun `タイヤ摩耗読み上げ文言を監視できる`() =
        runTest {
            every { repository.observeTyreWearReadoutText() } returns MutableStateFlow("ピットイン")
            val useCase = ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase(repository)

            assertEquals("ピットイン", useCase().first())
            verify(exactly = 1) { repository.observeTyreWearReadoutText() }
            confirmVerified(repository)
        }
}
