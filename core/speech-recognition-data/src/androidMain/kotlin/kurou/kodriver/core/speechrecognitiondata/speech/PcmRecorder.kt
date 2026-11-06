package kurou.kodriver.core.speechrecognitiondata.speech

/**
 * 16kHz・モノラル・16bit PCM のマイク入力を読み出す。プラットフォームの `AudioRecord` を直接呼ぶ部分をここに閉じ込め、
 * Repository側はフェイクで検証する。
 */
internal interface PcmRecorder : AutoCloseable {
    /** マイクの取り込みを始める。開けなければ例外を投げる。 */
    fun start()

    /** [buffer] に読み込めたバイト数を返す。音声が届くまで待つ。読み取れなくなったら 0 以下を返す。 */
    fun read(buffer: ByteArray): Int
}
