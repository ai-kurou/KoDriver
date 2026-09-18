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
import kurou.kodriver.domain.model.OverlayWindowBounds
import kurou.kodriver.domain.repository.OverlayWindowBoundsPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveOverlayWindowBoundsUseCaseTest {
    @MockK
    private lateinit var repository: OverlayWindowBoundsPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値を返す・保存済みの値を返す`() =
        runTest {
            val saved = OverlayWindowBounds(x = 10, y = 20, width = 300, height = 100)
            val state = MutableStateFlow(OverlayWindowBounds())
            every { repository.observeOverlayWindowBounds() } returns state
            coEvery { repository.saveOverlayWindowBounds(saved) } answers { state.update { saved } }
            val useCase = ObserveOverlayWindowBoundsUseCase(repository)

            assertEquals(OverlayWindowBounds(), useCase().first())

            repository.saveOverlayWindowBounds(saved)
            assertEquals(saved, useCase().first())

            verify(exactly = 2) { repository.observeOverlayWindowBounds() }
            coVerify(exactly = 1) { repository.saveOverlayWindowBounds(saved) }
            confirmVerified(repository)
        }
}
