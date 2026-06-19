package kurou.kodriver.domain.engine

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutStartSoundType

interface TextToSpeechEngine {
    /** 現在再生中のイベントが属する ReadoutItemKey。再生していない場合は null。 */
    val currentReadoutItemKey: ReadoutItemKey?
    fun speak(event: SpeechEvent, queue: Boolean = false)
    fun stop()
    fun previewStartSound(type: ReadoutStartSoundType)
}
