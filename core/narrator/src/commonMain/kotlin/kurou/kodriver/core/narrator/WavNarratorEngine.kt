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
 * [WavNarratorEngine] が読み込む開始音のリソース群。開始音タイプ→ファイルパスのマップと、
 * それを読み込む startSoundResourceLoader をまとめたもの。
 */
data class WavResources<START_TYPE>(
    val startSoundTypeToFile: Map<START_TYPE, String>,
    val startSoundResourceLoader: suspend (String) -> ByteArray,
)

/**
 * 開始音のWAV再生とカスタム読み上げ（OS標準TTS）を扱うエンジンの共通実装。
 *
 * LMU / GT7 / ACE の各 narrator feature は、[resources] に開始音タイプ→WAVファイルパスのマップと
 * 読み込み関数を渡す。読み上げ対象のイベントは [isCustomSpeakEvent] で判定し、[customSpeak] で本文を読み上げる。
 * 本文のWAVはもう持たないため、[isCustomSpeakEvent] が false のイベントは再生対象外となる。
 * `domain.engine.TextToSpeechEngine` を実装する型（[EVENT] に `SpeechEvent`、[START_TYPE] に
 * `ReadoutStartSoundType`、[KEY] に `ReadoutItemKey` を割り当てたもの）は、`:core:domain` に依存する
 * 呼び出し側（各 narrator feature）が薄いアダプタとして用意する。core:narrator が `:core:domain` へ
 * 依存しないようにするため、イベント・キー種別をすべて型パラメータ化している。
 */
@Suppress("LongParameterList")
class WavNarratorEngine<EVENT, START_TYPE, KEY>(
    private val soundPlayer: SoundPlayer,
    private val resources: WavResources<START_TYPE>,
    private val eventToKey: (EVENT) -> KEY,
    defaultStartSoundType: START_TYPE,
    volumeFlow: Flow<Int> = flowOf(100),
    startSoundTypeFlow: Flow<START_TYPE> = flowOf(defaultStartSoundType),
    startSoundEnabledStatesFlow: Flow<Map<KEY, Boolean>> = flowOf(emptyMap()),
    /**
     * [isCustomSpeakEvent] で指定したイベントについて、本文を読み上げるフック。
     * [event] とアプリの読み上げ音量（0〜100）を渡す。開始音は通常通り再生した上でこの関数を呼ぶ。
     * 再生中・優先度判定・割り込み（[currentKey] / [stop]）は呼び出し元の [play] と同じコルーチン上で
     * 実行されるため、WAVと同じ仕組みでそのまま扱える。
     */
    private val customSpeak: (suspend (EVENT, Int) -> Unit)? = null,
    /** [customSpeak] で本文を読み上げるイベントの判定。false のイベントは再生対象外。 */
    private val isCustomSpeakEvent: (EVENT) -> Boolean = { false },
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) {
    @Volatile
    private var currentVolume: Int = 100

    @Volatile
    private var currentStartSoundType: START_TYPE = defaultStartSoundType

    @Volatile
    private var currentStartSoundEnabledStates: Map<KEY, Boolean> = emptyMap()

    @Volatile
    private var startSounds: Map<START_TYPE, ByteArray> = emptyMap()

    private var playJob: Job? = null

    // playJob はキューのチェーン最後尾しか指さないため、割り込み時に再生中・待機中の
    // ジョブをまとめてキャンセルできるよう、全再生ジョブをこの親 Job にぶら下げる。
    // 新しい再生を launch する前に、生存中の Job へ差し替える。
    private var playbackParent: Job = SupervisorJob()

    // 再生側の待機ジョブが割り込みでキャンセルされても、過去の停止完了待ちを保持する。
    private var stopBarrier: Job = Job().apply { complete() }

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
            // ロード完了後は不変のマップに差し替えるため、読み取り競合は無害
            startSounds = loadedStartSounds
        }
    }

    fun speak(
        event: EVENT,
        queue: Boolean = false,
    ) {
        val body = playbackBody(event) ?: return
        if (queue) {
            // stop() 直後で playbackParent がキャンセル済みのままだと、その配下へ launch した
            // 瞬間に子ジョブごとキャンセルされてしまうため、生存中でなければ差し替える。
            if (!playbackParent.isActive) playbackParent = SupervisorJob()
            val barrier = stopBarrier
            val previousJob = playJob
            playJob =
                scope.launch(playbackParent) {
                    barrier.join()
                    previousJob?.join()
                    play(event, body)
                }
            return
        }
        val barrier = cancelPlayback()
        playbackParent = SupervisorJob()
        playJob =
            scope.launch(playbackParent) {
                barrier.join()
                play(event, body)
            }
    }

    /**
     * [event] の本編（開始音の後に再生するもの）。[isCustomSpeakEvent] の対象は [customSpeak] による読み上げ。
     * 対象外のイベントは再生対象外として null を返す。引数は読み上げ音量（0〜100）。
     */
    private fun playbackBody(event: EVENT): (suspend (Int) -> Unit)? {
        if (!isCustomSpeakEvent(event)) return null
        return { volume -> customSpeak?.invoke(event, volume) }
    }

    private suspend fun play(
        event: EVENT,
        body: suspend (Int) -> Unit,
    ) {
        val key = eventToKey(event)
        _currentKey = key
        val vol = currentVolume
        val startSoundEnabled = currentStartSoundEnabledStates[key] ?: true
        if (startSoundEnabled) {
            startSounds[currentStartSoundType]?.let { soundPlayer.play(it, vol) }
        }
        body(vol)
        _currentKey = null
    }

    fun stop() {
        cancelPlayback()
    }

    // playbackParent.cancel() は SoundPlayer の停止処理を非同期にトリガーするだけで、
    // 呼び出した時点では前の再生がまだ鳴っている。過去のバリアと今回キャンセルした親 Job の
    // 完了を順に待つバリアを返し、次の再生はそれを join() してから開始する。
    // バリアは playbackParent 配下ではなく scope へ直接 launch するため、連続割り込みで
    // 再生側の待機ジョブがキャンセルされても、過去の停止完了待ちは失われない。
    //
    // ここでは playbackParent を新しい Job に差し替えない。stop() 後もキャンセル済みの親を
    // 保持し、新しい再生を実際に launch する直前に差し替える。停止処理中の親を空の親へ
    // 置き換えず、stop() を含む各呼び出しの停止完了待ちを stopBarrier に連鎖させる。
    private fun cancelPlayback(): Job {
        val cancelled = playbackParent
        cancelled.cancel()
        playJob = null
        // キャンセルされた play() は _currentKey のクリアまで進まないため、停止待ちで新しいジョブが
        // active になった間に古いキーが currentKey として見えないようここで消す。
        _currentKey = null
        val previous = stopBarrier
        stopBarrier =
            scope.launch {
                previous.join()
                cancelled.join()
            }
        return stopBarrier
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
