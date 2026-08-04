package kurou.kodriver.feature.acewindowsnarrator

import kurou.kodriver.core.narrator.WavNarratorEngine
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutStartSoundType

/**
 * `:core:narrator` の [WavNarratorEngine]（`SpeechEvent` / `ReadoutStartSoundType` / `ReadoutItemKey` を
 * 知らない汎用実装）を [TextToSpeechEngine] として公開するための薄いアダプタ。
 */
internal class AceWindowsWavNarratorEngine(
    private val engine: WavNarratorEngine<SpeechEvent, ReadoutStartSoundType, ReadoutItemKey>,
) : TextToSpeechEngine {
    override val currentReadoutItemKey: ReadoutItemKey?
        get() = engine.currentKey

    override fun speak(
        event: SpeechEvent,
        queue: Boolean,
    ) = engine.speak(event, queue)

    override fun stop() = engine.stop()

    override fun previewStartSound(type: ReadoutStartSoundType) = engine.previewStartSound(type)
}
