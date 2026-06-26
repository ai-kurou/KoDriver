package kurou.kodriver.feature.gt7ps5narrator

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
import kurou.kodriver.feature.gt7ps5narrator.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
internal class Gt7Ps5WavNarratorEngine(
    private val soundPlayer: SoundPlayer,
    startSoundTypeFlow: Flow<ReadoutStartSoundType> = flowOf(ReadoutStartSoundType.FORMULA_RADIO),
    private val resourceLoader: suspend (String) -> ByteArray = Res::readBytes,
    private val startSoundResourceLoader: suspend (String) -> ByteArray = ::readStartSoundBytes,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) : TextToSpeechEngine {

    @Volatile
    private var currentStartSoundType: ReadoutStartSoundType = ReadoutStartSoundType.FORMULA_RADIO

    private var sounds: Map<SpeechEvent, ByteArray> = emptyMap()
    private var startSounds: Map<ReadoutStartSoundType, ByteArray> = emptyMap()
    private var playJob: Job? = null

    @Volatile
    private var _currentReadoutItemKey: ReadoutItemKey? = null

    override val currentReadoutItemKey: ReadoutItemKey?
        get() = _currentReadoutItemKey.takeIf { playJob?.isActive == true }

    private val eventToFile: Map<SpeechEvent, String> = buildMap {
        put(SpeechEvent.MyBestLapFormal, "files/my_best_lap_formal.wav")
        put(SpeechEvent.MyBestLapCasual, "files/my_best_lap_casual.wav")
        for (laps in 1..MAX_REMAINING_FUEL_LAPS) {
            put(SpeechEvent.RemainingFuelLapsWarning(laps), "files/remaining_fuel_laps_$laps.wav")
        }
    }

    private val startSoundTypeToFile = mapOf(
        ReadoutStartSoundType.FORMULA_RADIO to "files/formula_radio.wav",
        ReadoutStartSoundType.ELECTRONIC_NOISE to "files/electronic_noise.wav",
    )

    init {
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
        if (soundPlayer.isPlaying) return
        playJob?.cancel()
        playJob = scope.launch {
            _currentReadoutItemKey = event.readoutItemKey
            startSounds[currentStartSoundType]?.let { soundPlayer.play(it) }
            soundPlayer.play(mainSound)
            _currentReadoutItemKey = null
        }
    }

    override fun stop() {
        playJob?.cancel()
        playJob = null
    }

    override fun previewStartSound(type: ReadoutStartSoundType) {
        val sound = startSounds[type] ?: return
        if (soundPlayer.isPlaying) return
        playJob?.cancel()
        playJob = scope.launch { soundPlayer.play(sound) }
    }

    internal companion object {
        const val MAX_REMAINING_FUEL_LAPS = 5
    }
}
