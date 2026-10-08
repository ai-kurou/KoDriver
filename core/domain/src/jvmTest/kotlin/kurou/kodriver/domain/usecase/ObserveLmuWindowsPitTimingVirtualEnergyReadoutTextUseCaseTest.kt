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

class ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCaseTest {
    private val repository: LmuWindowsPitTimingReadoutTextPreferencesRepository = mockk()

    @Test
    fun `バーチャルエナジー読み上げ文言を監視できる`() =
        runTest {
            every { repository.observeVirtualEnergyReadoutText() } returns MutableStateFlow("ピットイン")
            val useCase = ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase(repository)

            assertEquals("ピットイン", useCase().first())
            verify(exactly = 1) { repository.observeVirtualEnergyReadoutText() }
            confirmVerified(repository)
        }
}
