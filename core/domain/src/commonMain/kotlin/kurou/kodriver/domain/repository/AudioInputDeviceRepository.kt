package kurou.kodriver.domain.repository

import kurou.kodriver.domain.model.AudioInputDevice

/**
 * 音声入力（マイク）デバイスの一覧を返すRepository。
 */
public interface AudioInputDeviceRepository {
    /** 現在利用できる入力デバイス。見つからなければ空。呼び出しごとに取得し直す。 */
    public suspend fun getDevices(): List<AudioInputDevice>
}
