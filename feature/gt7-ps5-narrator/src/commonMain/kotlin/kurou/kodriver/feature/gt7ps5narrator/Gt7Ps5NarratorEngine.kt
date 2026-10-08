package kurou.kodriver.feature.gt7ps5narrator

import kurou.kodriver.core.narrator.NarratorEngine
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutStartSoundType

/**
 * `:core:narrator` の [NarratorEngine]（`SpeechEvent` / `ReadoutStartSoundType` / `ReadoutItemKey` を
 * 知らない汎用実装）を [TextToSpeechEngine] として公開するための薄いアダプタ。
 */
internal class Gt7Ps5NarratorEngine(
    private val engine: NarratorEngine<SpeechEvent, ReadoutStartSoundType, ReadoutItemKey>,
) : TextToSpeechEngine {
    override val currentReadoutItemKey: ReadoutItemKey?
        get() = engine.currentKey

    override fun speak(
        event: SpeechEvent,
        queue: Boolean,
    ) = engine.speak(event, queue)

    override fun stop() = engine.stop()

    override fun previewStartSound(type: ReadoutStartSoundType) = engine.previewStartSound(type)

    override suspend fun playStartSound(key: ReadoutItemKey) = engine.playStartSoundForKey(key)
}
