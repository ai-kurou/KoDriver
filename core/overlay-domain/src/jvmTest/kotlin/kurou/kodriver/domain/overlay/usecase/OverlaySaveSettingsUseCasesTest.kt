package kurou.kodriver.domain.overlay.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.overlay.model.OverlayTextSize
import kurou.kodriver.domain.overlay.model.OverlayWindowBounds
import kurou.kodriver.domain.overlay.repository.OverlayTextColorPreferencesRepository
import kurou.kodriver.domain.overlay.repository.OverlayTextSizePreferencesRepository
import kurou.kodriver.domain.overlay.repository.OverlayVisiblePreferencesRepository
import kurou.kodriver.domain.overlay.repository.OverlayWindowBoundsPreferencesRepository
import kotlin.test.Test

class OverlaySaveSettingsUseCasesTest {
    @Test
    fun `設定保存UseCaseはRepositoryの対応するメソッドへ委譲する`() =
        runTest {
            assertSaves<OverlayTextSizePreferencesRepository>(
                useCaseName = "SaveOverlayTextSizeUseCase",
                save = { saveOverlayTextSize(OverlayTextSize.SMALL) },
                useCase = { SaveOverlayTextSizeUseCase(it)(OverlayTextSize.SMALL) },
            )
            assertSaves<OverlayTextColorPreferencesRepository>(
                useCaseName = "SaveOverlayTextColorUseCase",
                save = { saveOverlayTextColor(0xFFFFFFFFL) },
                useCase = { SaveOverlayTextColorUseCase(it)(0xFFFFFFFFL) },
            )
            assertSaves<OverlayVisiblePreferencesRepository>(
                useCaseName = "SaveOverlayVisibleUseCase",
                save = { saveOverlayVisible(true) },
                useCase = { SaveOverlayVisibleUseCase(it)(true) },
            )
            assertSaves<OverlayWindowBoundsPreferencesRepository>(
                useCaseName = "SaveOverlayWindowBoundsUseCase",
                save = { saveOverlayWindowBounds(OverlayWindowBounds(x = 10, y = 20, width = 300, height = 100)) },
                useCase = {
                    SaveOverlayWindowBoundsUseCase(
                        it,
                    )(OverlayWindowBounds(x = 10, y = 20, width = 300, height = 100))
                },
            )
        }

    private suspend inline fun <reified R : Any> assertSaves(
        useCaseName: String,
        crossinline save: suspend R.() -> Unit,
        crossinline useCase: suspend (R) -> Unit,
    ) {
        val repository = mockk<R>(relaxUnitFun = true)
        try {
            useCase(repository)

            coVerify(exactly = 1) { repository.save() }
            confirmVerified(repository)
        } catch (e: AssertionError) {
            throw AssertionError("$useCaseName: ${e.message}", e)
        }
    }
}
