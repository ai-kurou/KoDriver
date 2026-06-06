package kurou.kodriver.feature.narrator

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

    private val textToFile = mapOf(
        "カーレフト" to "files/car_left.wav",
        "カーライト" to "files/car_right.wav",
    )

    init {
        scope.launch {
            val loaded = mutableMapOf<String, ByteArray>()
            textToFile.forEach { (text, path) ->
                try {
                    loaded[text] = Res.readBytes(path)
                } catch (_: Exception) {
                }
            }
            sounds = loaded
        }
    }

    override fun speak(text: String) {
        if (soundPlayer.isPlaying) return
        sounds[text]?.let { soundPlayer.play(it) }
    }

    override fun stop() = Unit
}
