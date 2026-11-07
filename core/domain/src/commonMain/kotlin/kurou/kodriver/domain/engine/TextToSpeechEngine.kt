package kurou.kodriver.domain.engine

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutStartSoundType

interface TextToSpeechEngine {
    /**
     * 現在再生中、またはキューで再生待ちの先頭イベントが属する ReadoutItemKey。
     * 再生待ちのイベントは、まだ音が鳴る前でもここに公開される。再生も待機もない場合は null。
     */
    val currentReadoutItemKey: ReadoutItemKey?

    /** [currentReadoutItemKey] のイベントが手動読み上げ由来か。再生も待機もない場合は false。 */
    val currentIsManual: Boolean get() = false

    /**
     * [isManual] が true の場合、手動読み上げとして再生する（[currentIsManual] に反映される）。
     * [playStartSound] が false の場合、本文の前の開始音を鳴らさない。
     */
    fun speak(
        event: SpeechEvent,
        queue: Boolean = false,
        isManual: Boolean = false,
        playStartSound: Boolean = true,
    )

    fun stop()

    fun previewStartSound(type: ReadoutStartSoundType)

    /**
     * [key] に紐づく開始音（現在の設定の [ReadoutStartSoundType]）を再生し、再生完了まで待つ。
     * OS標準TTSで本文を読み上げる前に、開始音を鳴らしたい場合に使う。
     */
    suspend fun playStartSound(key: ReadoutItemKey)
}
