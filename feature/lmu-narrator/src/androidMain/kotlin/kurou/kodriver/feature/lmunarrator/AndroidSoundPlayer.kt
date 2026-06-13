package kurou.kodriver.feature.lmunarrator

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

class AndroidSoundPlayer(private val context: Context) : SoundPlayer {
    private var currentPlayer: MediaPlayer? = null

    override val isPlaying: Boolean
        get() = try { currentPlayer?.isPlaying == true } catch (_: Exception) { false }

    override suspend fun play(bytes: ByteArray) = suspendCancellableCoroutine { cont ->
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
                if (cont.isActive) cont.resume(Unit)
            }
            currentPlayer = player
            player.prepareAsync()
            cont.invokeOnCancellation {
                player.release()
                temp.delete()
            }
        } catch (_: Exception) {
            cont.resume(Unit)
        }
    }
}
