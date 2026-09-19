package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kotlin.test.Test

class PreviewStartSoundUseCaseTest {
    private val ttsEngine: TextToSpeechEngine = mockk(relaxUnitFun = true)

    @Test
    fun `TextToSpeechEngineへプレビュー再生を委譲する`() {
        PreviewStartSoundUseCase(ttsEngine)(ReadoutStartSoundType.FORMULA_RADIO)

        verify(exactly = 1) { ttsEngine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO) }
        confirmVerified(ttsEngine)
    }
}
