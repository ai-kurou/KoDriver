package kurou.kodriver.feature.lmuwindowsnarrator

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kurou.kodriver.core.designsystem.readStartSoundBytes
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
    private val startSoundResourceLoader: suspend (String) -> ByteArray = ::readStartSoundBytes,
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

    // playJob はキューのチェーン最後尾しか指さないため、割り込み時に再生中・待機中の
    // ジョブをまとめてキャンセルできるよう、全再生ジョブをこの親 Job にぶら下げる。
    private var playbackParent: Job = SupervisorJob()

    @Volatile
    private var _currentReadoutItemKey: ReadoutItemKey? = null

    // playJob がアクティブな間だけ再生中のキーを返す。
    // キャンセル後に古いジョブが _currentReadoutItemKey を上書きしないよう playJob で二重確認する。
    override val currentReadoutItemKey: ReadoutItemKey?
        get() = _currentReadoutItemKey.takeIf { playJob?.isActive == true }

    private val eventToFile: Map<SpeechEvent, String> = buildMap {
        put(SpeechEvent.CarLeft, "files/car_left.wav")
        put(SpeechEvent.CarRight, "files/car_right.wav")
        put(SpeechEvent.LeftApproach, "files/left_approach.wav")
        put(SpeechEvent.RightApproach, "files/right_approach.wav")
        put(SpeechEvent.KeepLeft, "files/keep_left.wav")
        put(SpeechEvent.KeepRight, "files/keep_right.wav")
        put(SpeechEvent.LeftSustained, "files/left_sustained.wav")
        put(SpeechEvent.RightSustained, "files/right_sustained.wav")
        put(SpeechEvent.BlueFlag, "files/blue_flag.wav")
        put(SpeechEvent.YellowFlag, "files/yellow_flag.wav")
        put(SpeechEvent.FullCourseYellow, "files/full_course_yellow.wav")
        put(SpeechEvent.SessionStop, "files/session_stopped.wav")
        put(SpeechEvent.RedFlag, "files/red_flag.wav")
        put(SpeechEvent.Overheating, "files/gp2_gp2.wav")
        put(SpeechEvent.LmuWindowsMyBestLapFormal, "files/my_best_lap_formal.wav")
        put(SpeechEvent.LmuWindowsMyBestLapCasual, "files/my_best_lap_casual.wav")
        put(SpeechEvent.TyreOverheat, "files/tyre_overheat.wav")
        put(SpeechEvent.TyreCold, "files/tyre_cold.wav")
        for (laps in 0..MAX_REMAINING_VIRTUAL_ENERGY_LAPS) {
            put(SpeechEvent.RemainingVirtualEnergyLapsWarning(laps), "files/remaining_virtual_energy_laps_$laps.wav")
        }
    }

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
                    loadedStartSounds[type] = startSoundResourceLoader(path)
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
            playJob = scope.launch(playbackParent) {
                previousJob?.join()
                play(event, mainSound)
            }
            return
        }
        if (soundPlayer.isPlaying) return
        cancelPlayback()
        playJob = scope.launch(playbackParent) { play(event, mainSound) }
    }

    override fun stop() {
        cancelPlayback()
    }

    private fun cancelPlayback() {
        playbackParent.cancel()
        playbackParent = SupervisorJob()
        playJob = null
    }

    override fun previewStartSound(type: ReadoutStartSoundType) {
        val sound = startSounds[type] ?: return
        if (soundPlayer.isPlaying) return
        cancelPlayback()
        playJob = scope.launch(playbackParent) { soundPlayer.play(sound, currentVolume) }
    }

    private suspend fun play(event: SpeechEvent, mainSound: ByteArray) {
        _currentReadoutItemKey = event.readoutItemKey
        val vol = currentVolume
        startSounds[currentStartSoundType]?.let { soundPlayer.play(it, vol) }
        soundPlayer.play(mainSound, vol)
        _currentReadoutItemKey = null
    }

    internal companion object {
        const val MAX_REMAINING_VIRTUAL_ENERGY_LAPS = 5
    }
}
