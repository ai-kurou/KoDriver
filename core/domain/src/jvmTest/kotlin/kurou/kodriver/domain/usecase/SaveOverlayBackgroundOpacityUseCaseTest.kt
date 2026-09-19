package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.OVERLAY_BACKGROUND_OPACITY_MAX
import kurou.kodriver.domain.model.OVERLAY_BACKGROUND_OPACITY_MIN
import kurou.kodriver.domain.repository.OverlayBackgroundOpacityPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SaveOverlayBackgroundOpacityUseCaseTest {
    private val repository: OverlayBackgroundOpacityPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `0から100の値を保存できる`() =
        runTest {
            val useCase = SaveOverlayBackgroundOpacityUseCase(repository)

            useCase(OVERLAY_BACKGROUND_OPACITY_MIN)
            useCase(50)
            useCase(OVERLAY_BACKGROUND_OPACITY_MAX)

            coVerify(exactly = 1) { repository.saveOverlayBackgroundOpacity(OVERLAY_BACKGROUND_OPACITY_MIN) }
            coVerify(exactly = 1) { repository.saveOverlayBackgroundOpacity(50) }
            coVerify(exactly = 1) { repository.saveOverlayBackgroundOpacity(OVERLAY_BACKGROUND_OPACITY_MAX) }
            confirmVerified(repository)
        }

    @Test
    fun `0未満はIllegalArgumentExceptionをスローする`() =
        runTest {
            assertFailsWith<IllegalArgumentException> {
                SaveOverlayBackgroundOpacityUseCase(repository)(OVERLAY_BACKGROUND_OPACITY_MIN - 1)
            }

            confirmVerified(repository)
        }

    @Test
    fun `100超はIllegalArgumentExceptionをスローする`() =
        runTest {
            assertFailsWith<IllegalArgumentException> {
                SaveOverlayBackgroundOpacityUseCase(repository)(OVERLAY_BACKGROUND_OPACITY_MAX + 1)
            }

            confirmVerified(repository)
        }
}
