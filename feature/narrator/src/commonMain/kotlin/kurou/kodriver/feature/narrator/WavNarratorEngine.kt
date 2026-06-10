package kurou.kodriver.feature.narrator

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.feature.narrator.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
internal class WavNarratorEngine(
    private val soundPlayer: SoundPlayer,
) : TextToSpeechEngine {

    // ロード完了後は不変のマップに差し替えるため、読み取り競合は無害
    private var sounds: Map<String, ByteArray> = emptyMap()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var noiseSound: ByteArray? = null

    private val textToFile = mapOf(
        "カーレフト" to "files/car_left.wav",
        "カーライト" to "files/car_right.wav",
        "ブルーフラッグ" to "files/blue_flag.wav",
        "イエローフラッグ" to "files/yellow_flag.wav",
        "フルコースイエロー" to "files/full_cource_yellow.wav",
        "セッションストップ" to "files/session_stopped.wav",
    )

    init {
        scope.launch {
            val loaded = mutableMapOf<String, ByteArray>()
            textToFile.forEach { (text, path) ->
                try {
                    loaded[text] = Res.readBytes(path)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                }
            }
            sounds = loaded
            try {
                noiseSound = Res.readBytes("files/noise.wav")
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
        }
    }

    override fun speak(text: String) {
        if (soundPlayer.isPlaying) return
        val mainSound = sounds[text] ?: return
        scope.launch {
            noiseSound?.let { soundPlayer.play(it) }
            soundPlayer.play(mainSound)
        }
    }

    override fun stop() = Unit
}
