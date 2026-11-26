package kurou.kodriver.domain.overlay.usecase

import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.overlay.model.OverlayTextSize
import kurou.kodriver.domain.overlay.repository.OverlayBackgroundOpacityPreferencesRepository
import kurou.kodriver.domain.overlay.repository.OverlayTextColorPreferencesRepository
import kurou.kodriver.domain.overlay.repository.OverlayTextSizePreferencesRepository
import kotlin.test.Test

class OverlayActionUseCasesTest {
    @Test
    fun `プレビュー系UseCaseは設定と解除をそのまま委譲する`() {
        assertDelegates<OverlayBackgroundOpacityPreferencesRepository>(
            useCaseName = "PreviewOverlayBackgroundOpacityUseCase",
            call = { setPreviewOverlayBackgroundOpacity(80) },
            useCase = { PreviewOverlayBackgroundOpacityUseCase(it)(80) },
        )
        assertDelegates<OverlayBackgroundOpacityPreferencesRepository>(
            useCaseName = "PreviewOverlayBackgroundOpacityUseCase",
            call = { setPreviewOverlayBackgroundOpacity(null) },
            useCase = { PreviewOverlayBackgroundOpacityUseCase(it)(null) },
        )
        assertDelegates<OverlayTextColorPreferencesRepository>(
            useCaseName = "PreviewOverlayTextColorUseCase",
            call = { setPreviewOverlayTextColor(0xFFFFFFFFL) },
            useCase = { PreviewOverlayTextColorUseCase(it)(0xFFFFFFFFL) },
        )
        assertDelegates<OverlayTextColorPreferencesRepository>(
            useCaseName = "PreviewOverlayTextColorUseCase",
            call = { setPreviewOverlayTextColor(null) },
            useCase = { PreviewOverlayTextColorUseCase(it)(null) },
        )
        assertDelegates<OverlayTextSizePreferencesRepository>(
            useCaseName = "PreviewOverlayTextSizeUseCase",
            call = { setPreviewOverlayTextSize(OverlayTextSize.SMALL) },
            useCase = { PreviewOverlayTextSizeUseCase(it)(OverlayTextSize.SMALL) },
        )
        assertDelegates<OverlayTextSizePreferencesRepository>(
            useCaseName = "PreviewOverlayTextSizeUseCase",
            call = { setPreviewOverlayTextSize(null) },
            useCase = { PreviewOverlayTextSizeUseCase(it)(null) },
        )
    }

    private inline fun <reified R : Any> assertDelegates(
        useCaseName: String,
        crossinline call: R.() -> Unit,
        crossinline useCase: (R) -> Unit,
    ) {
        val repository = mockk<R>(relaxUnitFun = true)
        try {
            useCase(repository)

            verify(exactly = 1) { repository.call() }
            confirmVerified(repository)
        } catch (e: AssertionError) {
            throw AssertionError("$useCaseName: ${e.message}", e)
        }
    }
}
