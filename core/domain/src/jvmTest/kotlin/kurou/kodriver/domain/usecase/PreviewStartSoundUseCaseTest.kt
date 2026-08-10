package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kotlin.test.BeforeTest
import kotlin.test.Test

class PreviewStartSoundUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var ttsEngine: TextToSpeechEngine

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `TextToSpeechEngineへプレビュー再生を委譲する`() {
        PreviewStartSoundUseCase(ttsEngine)(ReadoutStartSoundType.FORMULA_RADIO)

        verify(exactly = 1) { ttsEngine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO) }
        confirmVerified(ttsEngine)
    }
}
