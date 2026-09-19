package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.OverlayWindowBounds
import kurou.kodriver.domain.repository.OverlayWindowBoundsPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveOverlayWindowBoundsUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: OverlayWindowBoundsPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `位置・サイズを保存できる`() =
        runTest {
            val useCase = SaveOverlayWindowBoundsUseCase(repository)
            val moved = OverlayWindowBounds(x = 10, y = 20, width = 300, height = 100)
            val resized = moved.copy(width = 800, height = 400)

            useCase(moved)
            useCase(resized)

            coVerify(exactly = 1) { repository.saveOverlayWindowBounds(moved) }
            coVerify(exactly = 1) { repository.saveOverlayWindowBounds(resized) }
            confirmVerified(repository)
        }
}
