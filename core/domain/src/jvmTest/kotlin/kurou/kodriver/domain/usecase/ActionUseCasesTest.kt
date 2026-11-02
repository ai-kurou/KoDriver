package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.DeviceVolumeRepository
import kurou.kodriver.domain.repository.ManualReadoutRequestRepository
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.repository.OverlayBackgroundOpacityPreferencesRepository
import kurou.kodriver.domain.repository.OverlayTextColorPreferencesRepository
import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository
import kurou.kodriver.domain.repository.SpeechSettingsSenderRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ActionUseCasesTest {
    @Test
    fun `suspend系の操作UseCaseはRepositoryの対応するメソッドへ委譲する`() =
        runTest {
            assertDelegatesSuspend<ManualReadoutSetPreferencesRepository>(
                useCaseName = "AddManualReadoutSetUseCase",
                call = { addSet(ManualReadoutSet("set-1", listOf("SESSION", "REMOVED_KEY", "SIMULATOR"))) },
                useCase = {
                    AddManualReadoutSetUseCase(
                        it,
                    )(ManualReadoutSet("set-1", listOf("SESSION", "REMOVED_KEY", "SIMULATOR")))
                },
            )
            assertDelegatesSuspend<ManualReadoutSetPreferencesRepository>(
                useCaseName = "UpdateManualReadoutSetUseCase",
                call = { updateSet(ManualReadoutSet("set-1", listOf("SESSION", "REMOVED_KEY", "SIMULATOR"))) },
                useCase = {
                    UpdateManualReadoutSetUseCase(
                        it,
                    )(ManualReadoutSet("set-1", listOf("SESSION", "REMOVED_KEY", "SIMULATOR")))
                },
            )
            assertDelegatesSuspend<ManualReadoutSetPreferencesRepository>(
                useCaseName = "DeleteManualReadoutSetUseCase",
                call = { deleteSet("set-1") },
                useCase = { DeleteManualReadoutSetUseCase(it)("set-1") },
            )
            assertDelegatesSuspend<TelemetryLogRepository>(
                useCaseName = "DeleteTelemetryLogUseCase",
                call = { deleteTelemetryLog(1L) },
                useCase = { DeleteTelemetryLogUseCase(it)(1L) },
            )
            assertDelegatesSuspend<ManualReadoutRequestRepository>(
                useCaseName = "RequestManualReadoutUseCase",
                call = { request(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1")) },
                useCase = { RequestManualReadoutUseCase(it)(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1")) },
            )
        }

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
        assertDelegates<TextToSpeechEngine>(
            useCaseName = "PreviewStartSoundUseCase",
            call = { previewStartSound(ReadoutStartSoundType.FORMULA_RADIO) },
            useCase = { PreviewStartSoundUseCase(it)(ReadoutStartSoundType.FORMULA_RADIO) },
        )
    }

    @Test
    fun `OpenWindowsSpeechSettingsUseCaseはWindowsの音声設定を開く処理を委譲する`() {
        assertDelegates<SpeechSettingsSenderRepository>(
            useCaseName = "OpenWindowsSpeechSettingsUseCase",
            call = { openWindowsSpeechSettings() },
            useCase = { OpenWindowsSpeechSettingsUseCase(it)() },
        )
    }

    @Test
    fun `GetDeviceVolumeUseCaseはRepositoryの値をそのまま返す`() =
        runTest {
            val repository: DeviceVolumeRepository = mockk()
            coEvery { repository.getVolume() } returns 42

            assertEquals(42, GetDeviceVolumeUseCase(repository)())

            coVerify(exactly = 1) { repository.getVolume() }
            confirmVerified(repository)
        }

    private suspend inline fun <reified R : Any> assertDelegatesSuspend(
        useCaseName: String,
        crossinline call: suspend R.() -> Unit,
        crossinline useCase: suspend (R) -> Unit,
    ) {
        val repository = mockk<R>(relaxUnitFun = true)
        try {
            useCase(repository)

            coVerify(exactly = 1) { repository.call() }
            confirmVerified(repository)
        } catch (e: AssertionError) {
            throw AssertionError("$useCaseName: ${e.message}", e)
        }
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
