package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * 読み上げ（開始音を含む）の再生状態を、シミュレーター横断で共有するRepository。
 * 音声認識が自分の読み上げ音声を拾わないよう、再生中は聞き取りを止めるために使う。
 */
interface SpeechPlaybackStateRepository {
    /** いずれかのシミュレーターの読み上げが再生中、または再生待ちか。 */
    val isActive: StateFlow<Boolean>

    /** [sourceId]（シミュレーターのID）の再生状態を更新する。 */
    fun update(
        sourceId: String,
        active: Boolean,
    )
}
