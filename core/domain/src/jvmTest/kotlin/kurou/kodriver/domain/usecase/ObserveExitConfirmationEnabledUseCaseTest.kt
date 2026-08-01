package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.ExitConfirmationEnabledRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class ObserveExitConfirmationEnabledUseCaseTest {
    @MockK
    private lateinit var repository: ExitConfirmationEnabledRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `終了確認の有効状態を監視できる`() =
        runBlocking {
            every { repository.exitConfirmationEnabled() } returns MutableStateFlow(true)
            val useCase = ObserveExitConfirmationEnabledUseCase(repository)

            assertTrue(useCase().first())
            verify(exactly = 1) { repository.exitConfirmationEnabled() }
            confirmVerified(repository)
        }
}
