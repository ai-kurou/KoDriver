package kurou.kodriver.feature.announcer

import android.content.Context
import android.media.MediaPlayer
import java.io.File

internal class AndroidSoundPlayer(private val context: Context) : SoundPlayer {
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
            }
            player.prepareAsync()
        } catch (_: Exception) {
        }
    }
}
