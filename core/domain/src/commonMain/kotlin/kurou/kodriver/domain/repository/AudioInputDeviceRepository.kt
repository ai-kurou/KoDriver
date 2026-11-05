package kurou.kodriver.domain.repository

import kurou.kodriver.domain.model.AudioInputDevice

/**
 * 音声入力（マイク）デバイスの一覧を返すRepository。
 */
interface AudioInputDeviceRepository {
    /** 現在利用できる入力デバイス。見つからなければ空。呼び出しごとに取得し直す。 */
    suspend fun getDevices(): List<AudioInputDevice>
}
