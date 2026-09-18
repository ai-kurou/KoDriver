package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.OverlayVisiblePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObserveOverlayVisibleUseCaseTest {
    @MockK
    private lateinit var repository: OverlayVisiblePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値を返す・保存済みの値を返す`() =
        runTest {
            val state = MutableStateFlow(true)
            every { repository.observeOverlayVisible() } returns state
            coEvery { repository.saveOverlayVisible(false) } answers { state.update { false } }
            val useCase = ObserveOverlayVisibleUseCase(repository)

            assertTrue(useCase().first())

            repository.saveOverlayVisible(false)
            assertFalse(useCase().first())

            verify(exactly = 2) { repository.observeOverlayVisible() }
            coVerify(exactly = 1) { repository.saveOverlayVisible(false) }
            confirmVerified(repository)
        }
}
