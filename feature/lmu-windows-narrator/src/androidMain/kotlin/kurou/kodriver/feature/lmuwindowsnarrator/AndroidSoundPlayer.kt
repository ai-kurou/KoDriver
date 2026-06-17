package kurou.kodriver.feature.lmuwindowsnarrator

import android.content.Context
import android.media.MediaPlayer
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

class AndroidSoundPlayer(private val context: Context) : SoundPlayer {
    private var currentPlayer: MediaPlayer? = null

    override val isPlaying: Boolean
        get() = try { currentPlayer?.isPlaying == true } catch (_: Exception) { false }

    override suspend fun play(bytes: ByteArray, volume: Int) {
        val temp = withContext(Dispatchers.IO) {
            File.createTempFile("snd_", ".wav", context.cacheDir).also { it.writeBytes(bytes) }
        }
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { cont ->
                try {
                    val player = MediaPlayer()
                    player.setDataSource(temp.absolutePath)
                    val v = (volume / 100.0f).coerceIn(0f, 1f)
                    player.setVolume(v, v)
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
                } catch (e: Exception) {
                    Sentry.captureException(e)
                    temp.delete()
                    cont.resume(Unit)
                }
            }
        }
    }
}
