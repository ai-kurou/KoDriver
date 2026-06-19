package kurou.kodriver.feature.lmuwindowsnarrator

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.feature.lmuwindowsnarrator.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
internal class LmuWindowsWavNarratorEngine(
    private val soundPlayer: SoundPlayer,
    volumeFlow: Flow<Int> = flowOf(100),
    startSoundTypeFlow: Flow<ReadoutStartSoundType> = flowOf(ReadoutStartSoundType.FORMULA_RADIO),
    private val resourceLoader: suspend (String) -> ByteArray = Res::readBytes,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) : TextToSpeechEngine {

    @Volatile
    private var currentVolume: Int = 100

    @Volatile
    private var currentStartSoundType: ReadoutStartSoundType = ReadoutStartSoundType.FORMULA_RADIO

    // ロード完了後は不変のマップに差し替えるため、読み取り競合は無害
    private var sounds: Map<SpeechEvent, ByteArray> = emptyMap()

    private var startSounds: Map<ReadoutStartSoundType, ByteArray> = emptyMap()

    private var playJob: Job? = null

    @Volatile
    private var _currentReadoutItemKey: ReadoutItemKey? = null

    // playJob がアクティブな間だけ再生中のキーを返す。
    // キャンセル後に古いジョブが _currentReadoutItemKey を上書きしないよう playJob で二重確認する。
    override val currentReadoutItemKey: ReadoutItemKey?
        get() = _currentReadoutItemKey.takeIf { playJob?.isActive == true }

    private val eventToFile = mapOf(
        SpeechEvent.CarLeft to "files/car_left.wav",
        SpeechEvent.CarRight to "files/car_right.wav",
        SpeechEvent.LeftApproach to "files/left_approach.wav",
        SpeechEvent.RightApproach to "files/right_approach.wav",
        SpeechEvent.BlueFlag to "files/blue_flag.wav",
        SpeechEvent.YellowFlag to "files/yellow_flag.wav",
        SpeechEvent.FullCourseYellow to "files/full_course_yellow.wav",
        SpeechEvent.SessionStop to "files/session_stopped.wav",
        SpeechEvent.Overheating to "files/gp2_gp2.wav",
    )

    private val startSoundTypeToFile = mapOf(
        ReadoutStartSoundType.FORMULA_RADIO to "files/formula_radio.wav",
        ReadoutStartSoundType.ELECTRONIC_NOISE to "files/electronic_noise.wav",
    )

    init {
        scope.launch { volumeFlow.collect { currentVolume = it } }
        scope.launch { startSoundTypeFlow.collect { currentStartSoundType = it } }
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
            val loadedStartSounds = mutableMapOf<ReadoutStartSoundType, ByteArray>()
            startSoundTypeToFile.forEach { (type, path) ->
                try {
                    loadedStartSounds[type] = resourceLoader(path)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    captureNarratorError(e)
                }
            }
            startSounds = loadedStartSounds
        }
    }

    override fun speak(event: SpeechEvent, queue: Boolean) {
        val mainSound = sounds[event] ?: return
        if (queue) {
            val previousJob = playJob
            playJob = scope.launch {
                previousJob?.join()
                play(event, mainSound)
            }
            return
        }
        if (soundPlayer.isPlaying) return
        playJob?.cancel()
        playJob = scope.launch { play(event, mainSound) }
    }

    override fun stop() {
        playJob?.cancel()
        playJob = null
    }

    override fun previewStartSound(type: ReadoutStartSoundType) {
        val sound = startSounds[type] ?: return
        if (soundPlayer.isPlaying) return
        playJob?.cancel()
        playJob = scope.launch { soundPlayer.play(sound, currentVolume) }
    }

    private suspend fun play(event: SpeechEvent, mainSound: ByteArray) {
        _currentReadoutItemKey = event.readoutItemKey
        val vol = currentVolume
        startSounds[currentStartSoundType]?.let { soundPlayer.play(it, vol) }
        soundPlayer.play(mainSound, vol)
        _currentReadoutItemKey = null
    }
}
