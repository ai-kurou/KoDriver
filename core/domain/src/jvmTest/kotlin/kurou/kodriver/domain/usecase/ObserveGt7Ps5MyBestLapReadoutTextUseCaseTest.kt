package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveGt7Ps5MyBestLapReadoutTextUseCaseTest {
    private val repository: Gt7Ps5MyBestLapPreferencesRepository = mockk()

    @Test
    fun `自己ベストラップ更新の読み上げ文言を監視できる`() =
        runTest {
            every { repository.observeReadoutText() } returns MutableStateFlow("更新{laptime}")
            val useCase = ObserveGt7Ps5MyBestLapReadoutTextUseCase(repository)

            assertEquals("更新{laptime}", useCase().first())
            verify(exactly = 1) { repository.observeReadoutText() }
            confirmVerified(repository)
        }
}
