package kurou.kodriver.feature.narrator

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.feature.narrator.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
internal class WavNarratorEngine(
    private val soundPlayer: SoundPlayer,
) : TextToSpeechEngine {

    // ロード完了後は不変のマップに差し替えるため、読み取り競合は無害
    private var sounds: Map<SpeechEvent, ByteArray> = emptyMap()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var noiseSound: ByteArray? = null

    override val currentReadoutItemKey: String?
        get() = if (soundPlayer.isPlaying) _currentReadoutItemKey else null

    @Volatile
    private var _currentReadoutItemKey: String? = null

    private val eventToFile = mapOf(
        SpeechEvent.CarLeft to "files/car_left.wav",
        SpeechEvent.CarRight to "files/car_right.wav",
        SpeechEvent.BlueFlag to "files/blue_flag.wav",
        SpeechEvent.YellowFlag to "files/yellow_flag.wav",
        SpeechEvent.FullCourseYellow to "files/full_course_yellow.wav",
        SpeechEvent.SessionStop to "files/session_stopped.wav",
    )

    init {
        scope.launch {
            val loaded = mutableMapOf<SpeechEvent, ByteArray>()
            eventToFile.forEach { (event, path) ->
                try {
                    loaded[event] = Res.readBytes(path)
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

    override fun speak(event: SpeechEvent) {
        if (soundPlayer.isPlaying) return
        val mainSound = sounds[event] ?: return
        scope.launch {
            _currentReadoutItemKey = event.readoutItemKey
            try {
                noiseSound?.let { soundPlayer.play(it) }
                soundPlayer.play(mainSound)
            } finally {
                _currentReadoutItemKey = null
            }
        }
    }

    override fun stop() = Unit
}
