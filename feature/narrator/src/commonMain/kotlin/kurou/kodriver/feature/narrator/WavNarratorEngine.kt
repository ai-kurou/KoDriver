package kurou.kodriver.feature.narrator

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.feature.narrator.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
internal class WavNarratorEngine(
    private val soundPlayer: SoundPlayer,
    private val resourceLoader: suspend (String) -> ByteArray = Res::readBytes,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) : TextToSpeechEngine {

    // ロード完了後は不変のマップに差し替えるため、読み取り競合は無害
    private var sounds: Map<SpeechEvent, ByteArray> = emptyMap()

    private var noiseSound: ByteArray? = null

    private var playJob: Job? = null

    @Volatile
    private var _currentReadoutItemKey: String? = null

    // playJob がアクティブな間だけ再生中のキーを返す。
    // キャンセル後に古いジョブが _currentReadoutItemKey を上書きしないよう playJob で二重確認する。
    override val currentReadoutItemKey: String?
        get() = _currentReadoutItemKey.takeIf { playJob?.isActive == true }

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
                    loaded[event] = resourceLoader(path)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                }
            }
            sounds = loaded
            try {
                noiseSound = resourceLoader("files/noise.wav")
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
        }
    }

    override fun speak(event: SpeechEvent) {
        val mainSound = sounds[event] ?: return
        playJob?.cancel()
        playJob = scope.launch {
            _currentReadoutItemKey = event.readoutItemKey
            noiseSound?.let { soundPlayer.play(it) }
            soundPlayer.play(mainSound)
            _currentReadoutItemKey = null
        }
    }

    override fun stop() {
        playJob?.cancel()
        playJob = null
    }
}
