package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.AceWindowsMyBestLapPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveAceWindowsMyBestLapReadoutTextUseCaseTest {
    private val repository: AceWindowsMyBestLapPreferencesRepository = mockk()

    @Test
    fun `自己ベストラップ更新の読み上げ文言を監視できる`() =
        runTest {
            every { repository.observeReadoutText() } returns MutableStateFlow("更新{laptime}")
            val useCase = ObserveAceWindowsMyBestLapReadoutTextUseCase(repository)

            assertEquals("更新{laptime}", useCase().first())
            verify(exactly = 1) { repository.observeReadoutText() }
            confirmVerified(repository)
        }
}
