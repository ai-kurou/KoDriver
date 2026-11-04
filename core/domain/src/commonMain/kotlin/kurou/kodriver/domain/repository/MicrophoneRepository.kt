package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.model.MicrophoneAudioEvent

/**
 * マイクから音声（PCM）を取得するRepository。
 */
interface MicrophoneRepository {
    /**
     * [device] のマイクから音声を取得する cold Flow。収集を開始するとマイクを開き、収集のキャンセルで閉じる。
     * [device] が null のときはOSの既定の入力デバイスを使う。
     * 続行できない場合は [MicrophoneAudioEvent.Failed] を流して完了する。
     */
    fun observeAudio(device: AudioInputDevice?): Flow<MicrophoneAudioEvent>
}
