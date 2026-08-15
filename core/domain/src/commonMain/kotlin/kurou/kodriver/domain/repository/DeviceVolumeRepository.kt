package kurou.kodriver.domain.repository

/**
 * 端末（OS）のマスター音量を取得・設定するRepository。KoDriver自体のアナウンス音量設定
 * （[SoundVolumePreferencesRepository]）とは異なり、読み上げが実際に聞こえるかどうかに関わる
 * OS側の再生音量を扱う。値は0-100のパーセンテージで表す。
 */
interface DeviceVolumeRepository {
    suspend fun getVolume(): Int

    suspend fun setVolume(volume: Int)
}
