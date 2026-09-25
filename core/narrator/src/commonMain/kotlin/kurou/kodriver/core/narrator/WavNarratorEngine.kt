package kurou.kodriver.core.narrator

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile

/**
 * [WavNarratorEngine] が読み込む WAV リソース群。イベント→WAVファイルパスのマップ・開始音タイプ→ファイルパスの
 * マップ・それぞれを読み込む resourceLoader をまとめたもの。
 */
data class WavResources<EVENT, START_TYPE>(
    val eventToFile: Map<EVENT, String>,
    val startSoundTypeToFile: Map<START_TYPE, String>,
    val resourceLoader: suspend (String) -> ByteArray,
    val startSoundResourceLoader: suspend (String) -> ByteArray,
)

/**
 * WAV 音声を読み上げるエンジンの共通実装。
 *
 * LMU / GT7 / ACE の各 narrator feature は、[resources] にイベント→WAVファイルパスのマップと
 * 自身の compose resources（`Res::readBytes`）を渡すだけで、このエンジンをそのまま利用できる。
 * `domain.engine.TextToSpeechEngine` を実装する型（[EVENT] に `SpeechEvent`、[START_TYPE] に
 * `ReadoutStartSoundType`、[KEY] に `ReadoutItemKey` を割り当てたもの）は、`:core:domain` に依存する
 * 呼び出し側（各 narrator feature）が薄いアダプタとして用意する。core:narrator が `:core:domain` へ
 * 依存しないようにするため、イベント・キー種別をすべて型パラメータ化している。
 */
@Suppress("LongParameterList")
class WavNarratorEngine<EVENT, START_TYPE, KEY>(
    private val soundPlayer: SoundPlayer,
    private val resources: WavResources<EVENT, START_TYPE>,
    private val eventToKey: (EVENT) -> KEY,
    defaultStartSoundType: START_TYPE,
    volumeFlow: Flow<Int> = flowOf(100),
    startSoundTypeFlow: Flow<START_TYPE> = flowOf(defaultStartSoundType),
    startSoundEnabledStatesFlow: Flow<Map<KEY, Boolean>> = flowOf(emptyMap()),
    /**
     * [event] を渡し、WAV の代わりにこの関数側で読み上げを行わせたい場合に使うフック。
     * `true` を返すと、開始音は通常通り再生した上でWAV本編（[EVENT] に対応する [sounds]）の再生をスキップする。
     * `false`（既定）を返す、またはこのフック自体を渡さない場合は、常にWAVで読み上げる。
     * 再生中・優先度判定・割り込み（[currentKey] / [stop]）は呼び出し元の [play] と同じコルーチン上で
     * 実行されるため、WAVと同じ仕組みでそのまま扱える。
     */
    private val customSpeak: (suspend (EVENT) -> Boolean)? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) {
    @Volatile
    private var currentVolume: Int = 100

    @Volatile
    private var currentStartSoundType: START_TYPE = defaultStartSoundType

    @Volatile
    private var currentStartSoundEnabledStates: Map<KEY, Boolean> = emptyMap()

    @Volatile
    private var sounds: Map<EVENT, ByteArray> = emptyMap()

    @Volatile
    private var startSounds: Map<START_TYPE, ByteArray> = emptyMap()

    private var playJob: Job? = null

    // playJob はキューのチェーン最後尾しか指さないため、割り込み時に再生中・待機中の
    // ジョブをまとめてキャンセルできるよう、全再生ジョブをこの親 Job にぶら下げる。
    // queue=true の speak() は常にこの Job 配下へ launch するため、cancelPlayback() は
    // 呼び出しのたびにここを生存中の新しい Job へ差し替える。
    private var playbackParent: Job = SupervisorJob()

    @Volatile
    private var _currentKey: KEY? = null

    // playJob がアクティブな間だけ再生中のキーを返す。
    // キャンセル後に古いジョブが _currentKey を上書きしないよう playJob で二重確認する。
    val currentKey: KEY?
        get() = _currentKey.takeIf { playJob?.isActive == true }

    init {
        scope.launch { volumeFlow.collect { currentVolume = it } }
        scope.launch { startSoundTypeFlow.collect { currentStartSoundType = it } }
        scope.launch { startSoundEnabledStatesFlow.collect { currentStartSoundEnabledStates = it } }
        scope.launch {
            val loaded = mutableMapOf<EVENT, ByteArray>()
            resources.eventToFile.forEach { (event, path) ->
                try {
                    loaded[event] = resources.resourceLoader(path)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    captureNarratorError(e)
                }
            }
            // ロード完了後は不変のマップに差し替えるため、読み取り競合は無害
            sounds = loaded
            val loadedStartSounds = mutableMapOf<START_TYPE, ByteArray>()
            resources.startSoundTypeToFile.forEach { (type, path) ->
                try {
                    loadedStartSounds[type] = resources.startSoundResourceLoader(path)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    captureNarratorError(e)
                }
            }
            startSounds = loadedStartSounds
        }
    }

    fun speak(
        event: EVENT,
        queue: Boolean = false,
    ) {
        val mainSound = sounds[event] ?: return
        if (queue) {
            // stop() 直後で playbackParent がキャンセル済みのままだと、その配下へ launch した
            // 瞬間に子ジョブごとキャンセルされてしまうため、生存中でなければ差し替える。
            if (!playbackParent.isActive) playbackParent = SupervisorJob()
            val previousJob = playJob
            playJob =
                scope.launch(playbackParent) {
                    previousJob?.join()
                    play(event, mainSound)
                }
            return
        }
        val barrier = cancelPlayback()
        playbackParent = SupervisorJob()
        playJob =
            scope.launch(playbackParent) {
                barrier.join()
                play(event, mainSound)
            }
    }

    private suspend fun play(
        event: EVENT,
        mainSound: ByteArray,
    ) {
        val key = eventToKey(event)
        _currentKey = key
        val vol = currentVolume
        val startSoundEnabled = currentStartSoundEnabledStates[key] ?: true
        if (startSoundEnabled) {
            startSounds[currentStartSoundType]?.let { soundPlayer.play(it, vol) }
        }
        val spokenByCustomSpeak = customSpeak?.invoke(event) == true
        if (!spokenByCustomSpeak) {
            soundPlayer.play(mainSound, vol)
        }
        _currentKey = null
    }

    fun stop() {
        cancelPlayback()
    }

    // playbackParent.cancel() は SoundPlayer の停止処理を非同期にトリガーするだけで、
    // 呼び出した時点では前の再生がまだ鳴っている。次に本当に再生を始めてよいタイミングは
    // ここでキャンセルした Job が完了（＝停止処理が完了）した後なので、その Job を戻り値として
    // 返し、呼び出し元（speak()/previewStartSound()）はそれを join() してから再生する。
    //
    // ここでは playbackParent を新しい Job に差し替えない。stop() は「今キャンセルすべき Job」を
    // 返すだけで、次に speak()/previewStartSound() が呼ばれるまで playbackParent はキャンセル済み
    // のまま保持される。こうすることで、stop() の直後に speak() が呼ばれた場合でも、
    // speak() 自身の cancelPlayback() がその「まだ停止処理中の Job」を正しく再取得して待てる。
    // （stop() 側で先に新しい空の Job へ差し替えてしまうと、speak() 側は空の Job しか
    // 参照できず、停止処理の完了を待たずに次の音声が重複再生されてしまう。）
    // 差し替え自体は、新しい再生を実際に launch する直前（speak()/previewStartSound() 側）で行う。
    private fun cancelPlayback(): Job {
        val cancelled = playbackParent
        cancelled.cancel()
        playJob = null
        return cancelled
    }

    fun previewStartSound(type: START_TYPE) {
        val sound = startSounds[type] ?: return
        val barrier = cancelPlayback()
        playbackParent = SupervisorJob()
        playJob =
            scope.launch(playbackParent) {
                barrier.join()
                soundPlayer.play(sound, currentVolume)
            }
    }

    /**
     * [key] に紐づく開始音（現在選択中の [START_TYPE]）を再生し、再生完了まで待つ。
     * WAV以外（OS標準TTS等）で本文を読み上げる前に、収録音声と同じ開始音を鳴らしたい場合に使う。
     * [key] の開始音が無効化されている場合、または開始音が読み込めていない場合は何もしない。
     */
    suspend fun playStartSoundForKey(key: KEY) {
        val startSoundEnabled = currentStartSoundEnabledStates[key] ?: true
        if (!startSoundEnabled) return
        val sound = startSounds[currentStartSoundType] ?: return
        val barrier = cancelPlayback()
        playbackParent = SupervisorJob()
        val job =
            scope.launch(playbackParent) {
                barrier.join()
                soundPlayer.play(sound, currentVolume)
            }
        playJob = job
        job.join()
    }
}
