package kurou.kodriver.feature.narrator

import android.content.Context
import android.media.MediaPlayer
import java.io.File

class AndroidSoundPlayer(private val context: Context) : SoundPlayer {
    private var currentPlayer: MediaPlayer? = null

    override val isPlaying: Boolean
        get() = try { currentPlayer?.isPlaying == true } catch (_: Exception) { false }

    override fun play(bytes: ByteArray) {
        try {
            val temp = File.createTempFile("snd_", ".wav", context.cacheDir)
            temp.writeBytes(bytes)
            val player = MediaPlayer()
            player.setDataSource(temp.absolutePath)
            player.setOnPreparedListener { it.start() }
            player.setOnCompletionListener {
                it.release()
                temp.delete()
                currentPlayer = null
            }
            currentPlayer = player
            player.prepareAsync()
        } catch (_: Exception) {
        }
    }
}
