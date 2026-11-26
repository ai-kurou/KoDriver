package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.microphone.model.toMicrophoneCutQuietSoundThreshold
import kurou.kodriver.domain.microphone.repository.MicrophoneCutQuietSoundPreferencesRepository
import kotlin.coroutines.coroutineContext

/** 「小さい音をカット」の段階値を、音量ゲートの閾値（[pcmLevel] の 0.0〜1.0）にする。 */
internal fun Int.toVolumeGateThreshold(): Float = toMicrophoneCutQuietSoundThreshold()

/**
 * 保存済みの「小さい音をカット」の設定を音量ゲートの閾値と「コマンド受付中もカットするか」として購読しながら [block] を実行する。
 * 設定を変えると、聞き取りを続けたまま次の断片から新しい値になる。[block] を抜けたら購読をやめる。
 */
internal suspend fun <T> MicrophoneCutQuietSoundPreferencesRepository.withVolumeGate(
    block: suspend (threshold: StateFlow<Float>, cutInCommand: StateFlow<Boolean>) -> T,
): T =
    coroutineScope {
        val threshold = observeMicrophoneCutQuietSoundLevel().map { it.toVolumeGateThreshold() }.stateIn(this)
        val cutInCommand = observeMicrophoneCutQuietSoundInCommand().stateIn(this)
        try {
            block(threshold, cutInCommand)
        } finally {
            coroutineContext.cancelChildren()
        }
    }
