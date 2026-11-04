package kurou.kodriver.data.microphone

/**
 * OSの音声入力を扱う境界。実機のマイクに依存する部分をここに閉じ込め、Repository側はフェイクで検証する。
 */
internal interface AudioInputSource {
    /** 取得に使える入力デバイスの名前。 */
    fun deviceNames(): List<String>

    /** [deviceName] のデバイスを開く。null は既定の入力デバイス。見つからない・開けないときは null。 */
    fun open(deviceName: String?): AudioCapture?
}

/**
 * 開いた入力デバイス。[close] で取得を止める。
 */
internal interface AudioCapture : AutoCloseable {
    /** 最大で [buffer] の大きさまで読み込み、読み込んだバイト数を返す（ブロックする）。デバイスが閉じた・使えなくなったときは -1。 */
    fun read(buffer: ByteArray): Int
}
