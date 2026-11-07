package kurou.kodriver.core.speechrecognitiondata.speech

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kurou.kodriver.domain.model.MICROPHONE_SAMPLE_RATE_HZ

/**
 * Android の [AudioRecord] で [PcmRecorder] を実現する。呼び出し元でマイクの権限を確認してから使う。
 * プラットフォームのAPIを直接呼ぶ薄いアダプターのため、単体テストの対象にしない。
 */
internal class AudioRecordPcmRecorder : PcmRecorder {
    @Volatile
    private var record: AudioRecord? = null

    @SuppressLint("MissingPermission")
    override fun start() {
        val minBufferSize =
            AudioRecord.getMinBufferSize(
                MICROPHONE_SAMPLE_RATE_HZ,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            )
        check(minBufferSize > 0) { "マイクの入力形式に対応していません" }
        val created =
            AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                MICROPHONE_SAMPLE_RATE_HZ,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize * BUFFER_SIZE_FACTOR,
            )
        record = created
        check(created.state == AudioRecord.STATE_INITIALIZED) { "マイクを初期化できません" }
        created.startRecording()
    }

    override fun read(buffer: ByteArray): Int = record?.read(buffer, 0, buffer.size) ?: -1

    // 読み取りで待っているスレッドに対し、別スレッドから録音を止めて戻させる（解放済みなら何もしない）
    override fun interrupt() {
        runCatching { record?.stop() }
    }

    override fun close() {
        record?.release()
        record = null
    }

    private companion object {
        /** 読み取りが遅れても取りこぼさないよう、最小のバッファーに掛ける倍率。 */
        const val BUFFER_SIZE_FACTOR = 4
    }
}
