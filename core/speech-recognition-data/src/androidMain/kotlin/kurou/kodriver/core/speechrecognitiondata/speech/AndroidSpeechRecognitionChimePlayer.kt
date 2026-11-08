package kurou.kodriver.core.speechrecognitiondata.speech

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kurou.kodriver.domain.engine.SpeechRecognitionChimePlayer

/**
 * Android版の確認音プレイヤー。[wakeWordChimePcm] を静的な [AudioTrack] に載せて鳴らし、鳴り終わるまで待つ。
 *
 * [AudioTrack] の生成は [createTrack] で差し替えられる（実機の出力が無いテスト環境で検証するため）。
 */
internal class AndroidSpeechRecognitionChimePlayer(
    private val createTrack: (ShortArray) -> AudioTrack = ::buildStaticChimeTrack,
) : SpeechRecognitionChimePlayer {
    override suspend fun play() {
        var created: AudioTrack? = null
        try {
            // 波形の生成とAudioTrackの確保はメインスレッドを塞がないようIOで行う。
            // 生成中に取り消されると withContext は結果を捨てるため、作ったトラックはブロック内で控えて finally で解放する
            withContext(Dispatchers.IO) { created = createTrack(wakeWordChimePcm()) }
            val track = checkNotNull(created)
            track.play()
            // 静的トラックは再生完了を通知しないため、再生時間だけ待ってから解放する
            delay(WAKE_WORD_CHIME_DURATION_MS.toLong())
        } finally {
            created?.release()
        }
    }
}

/** UI効果音の用途で、再生前に全サンプルを書き込んだ静的トラックを作る。 */
private fun buildStaticChimeTrack(pcm: ShortArray): AudioTrack {
    val track =
        AudioTrack
            .Builder()
            .setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            ).setAudioFormat(
                AudioFormat
                    .Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(WAKE_WORD_CHIME_SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            ).setBufferSizeInBytes(pcm.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
    track.write(pcm, 0, pcm.size)
    return track
}
