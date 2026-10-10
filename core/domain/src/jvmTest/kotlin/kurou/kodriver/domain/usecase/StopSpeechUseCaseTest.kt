package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test

class StopSpeechUseCaseTest {
    private val engine: TextToSpeechEngine = mockk(relaxUnitFun = true)
    private val key = ReadoutItemKey.LmuWindows.TyreWear.Root

    @Test
    fun `再生中の項目が指定キーと一致する場合は停止する`() {
        every { engine.currentReadoutItemKey } returns key

        StopSpeechUseCase(engine)(key)

        verify(exactly = 1) { engine.currentReadoutItemKey }
        verify(exactly = 1) { engine.stop() }
        confirmVerified(engine)
    }

    @Test
    fun `再生が終わっている場合は停止しない`() {
        every { engine.currentReadoutItemKey } returns null

        StopSpeechUseCase(engine)(key)

        verify(exactly = 1) { engine.currentReadoutItemKey }
        confirmVerified(engine)
    }

    @Test
    fun `別項目の読み上げ中は停止しない`() {
        every { engine.currentReadoutItemKey } returns ReadoutItemKey.LmuWindows.Flag.Root

        StopSpeechUseCase(engine)(key)

        verify(exactly = 1) { engine.currentReadoutItemKey }
        confirmVerified(engine)
    }
}
