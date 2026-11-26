package kurou.kodriver.domain.microphone.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.microphone.model.MicrophoneAudioEvent
import kurou.kodriver.domain.model.AudioInputDevice

/**
 * マイクから音声（PCM）を取得するRepository。
 */
public interface MicrophoneRepository {
    /**
     * [device] のマイクから音声を取得する cold Flow。収集を開始するとマイクを開き、収集のキャンセルで閉じる。
     * [device] が null のときはOSの既定の入力デバイスを使う。
     * 続行できない場合は [MicrophoneAudioEvent.Failed] を流して完了する。
     */
    public fun observeAudio(device: AudioInputDevice?): Flow<MicrophoneAudioEvent>
}
