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
import kurou.kodriver.domain.repository.OverlayBackgroundOpacityPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveOverlayBackgroundOpacityUseCaseTest {
    @MockK
    private lateinit var repo: OverlayBackgroundOpacityPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値を返す・保存済みの値を返す`() =
        runTest {
            val state = MutableStateFlow(80)
            every { repo.observeOverlayBackgroundOpacity() } returns state
            listOf(50).forEach { opacity ->
                coEvery { repo.saveOverlayBackgroundOpacity(opacity) } answers { state.update { opacity } }
            }
            val useCase = ObserveOverlayBackgroundOpacityUseCase(repo)

            assertEquals(80, useCase().first())

            repo.saveOverlayBackgroundOpacity(50)
            assertEquals(50, useCase().first())

            verify(exactly = 2) { repo.observeOverlayBackgroundOpacity() }
            coVerify(exactly = 1) { repo.saveOverlayBackgroundOpacity(50) }
            confirmVerified(repo)
        }
}
