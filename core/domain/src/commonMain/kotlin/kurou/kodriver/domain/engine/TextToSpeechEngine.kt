package kurou.kodriver.domain.engine

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutStartSoundType

interface TextToSpeechEngine {
    /** 現在再生中のイベントが属する ReadoutItemKey。再生していない場合は null。 */
    val currentReadoutItemKey: ReadoutItemKey?

    fun speak(
        event: SpeechEvent,
        queue: Boolean = false,
    )

    fun stop()

    fun previewStartSound(type: ReadoutStartSoundType)

    /**
     * [key] に紐づく開始音（現在の設定の [ReadoutStartSoundType]）を再生し、再生完了まで待つ。
     * OS標準TTSで本文を読み上げる前に、開始音を鳴らしたい場合に使う。
     */
    suspend fun playStartSound(key: ReadoutItemKey)
}
