package kurou.kodriver.feature.gt7ps5narrator

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.feature.gt7ps5narrator.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
internal class Gt7Ps5WavNarratorEngine(
    private val soundPlayer: SoundPlayer,
    private val resourceLoader: suspend (String) -> ByteArray = Res::readBytes,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) : TextToSpeechEngine {

    private var sounds: Map<SpeechEvent, ByteArray> = emptyMap()
    private var playJob: Job? = null

    @Volatile
    private var _currentReadoutItemKey: ReadoutItemKey? = null

    override val currentReadoutItemKey: ReadoutItemKey?
        get() = _currentReadoutItemKey.takeIf { playJob?.isActive == true }

    private val eventToFile = mapOf(
        SpeechEvent.MyBestLapFormal to "files/my_best_lap_formal.wav",
        SpeechEvent.MyBestLapCasual to "files/my_best_lap_casual.wav",
    )

    init {
        scope.launch {
            val loaded = mutableMapOf<SpeechEvent, ByteArray>()
            eventToFile.forEach { (event, path) ->
                try {
                    loaded[event] = resourceLoader(path)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    captureNarratorError(e)
                }
            }
            sounds = loaded
        }
    }

    override fun speak(event: SpeechEvent, queue: Boolean) {
        val sound = sounds[event] ?: return
        if (soundPlayer.isPlaying) return
        playJob?.cancel()
        playJob = scope.launch {
            _currentReadoutItemKey = event.readoutItemKey
            soundPlayer.play(sound)
            _currentReadoutItemKey = null
        }
    }

    override fun stop() {
        playJob?.cancel()
        playJob = null
    }

    override fun previewStartSound(type: ReadoutStartSoundType) = Unit
}
