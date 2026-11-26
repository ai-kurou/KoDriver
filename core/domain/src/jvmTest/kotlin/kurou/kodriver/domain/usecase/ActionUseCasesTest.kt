package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.repository.DeviceVolumeRepository
import kurou.kodriver.domain.repository.SpeechSettingsSenderRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ActionUseCasesTest {
    @Test
    fun `プレビュー系UseCaseは設定と解除をそのまま委譲する`() {
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
