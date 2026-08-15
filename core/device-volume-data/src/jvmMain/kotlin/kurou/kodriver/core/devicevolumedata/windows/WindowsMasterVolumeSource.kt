package kurou.kodriver.core.devicevolumedata.windows

/**
 * 既定のオーディオ出力デバイスのマスター音量（0.0-1.0のスカラー値）を取得・設定する。
 * 実装はJNA経由でWindows Core Audio（WASAPI）のCOMインターフェースを直接呼び出すため、
 * テストでは [WasapiMasterVolumeSource] の代わりにFakeへ差し替える。
 */
interface WindowsMasterVolumeSource {
    fun getScalarVolume(): Float

    fun setScalarVolume(level: Float)
}
