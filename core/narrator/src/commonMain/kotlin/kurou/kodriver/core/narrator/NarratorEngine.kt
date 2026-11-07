package kurou.kodriver.core.narrator

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile

/**
 * [NarratorEngine] が読み込む開始音のリソース群。開始音タイプ→ファイルパスのマップと、
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
class NarratorEngine<EVENT, START_TYPE, KEY>(
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
    private val customSpeak: suspend (EVENT, Int) -> Unit,
    /** [customSpeak] で本文を読み上げるイベントの判定。false のイベントは再生対象外。 */
    private val isCustomSpeakEvent: (EVENT) -> Boolean = { false },
    /** 手動読み上げ由来のイベントかの判定。再生中のイベントが手動かを [currentIsManual] で参照するために使う。 */
    private val isManualEvent: (EVENT) -> Boolean = { false },
    /**
     * 再生中または再生待ちのイベントがあるかが変わったときに、変わった後の値で呼ぶ。
     * 音声認識が自分の読み上げを拾わないよう、再生中は聞き取りを止めるために使う。値が変わらない更新では呼ばない。
     */
    private val onPlaybackActiveChanged: (Boolean) -> Unit = {},
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
    // 新しい再生を launch する前に、生存中のスコープへ差し替える。
    // launch に Job を直接渡すと構造化並行性が崩れるため、Job を持つ CoroutineScope として保持する。
    private var playbackScope: CoroutineScope = newPlaybackScope()

    // 再生側の待機ジョブが割り込みでキャンセルされても、過去の停止完了待ちを保持する。
    private var stopBarrier: Job = Job().apply { complete() }

    @Volatile
    private var _currentKey: KEY? = null
        set(value) {
            val wasActive = field != null
            field = value
            if (wasActive != (value != null)) onPlaybackActiveChanged(value != null)
        }

    // playJob がアクティブな間だけ再生中のキーを返す。
    // キャンセル後に古いジョブが _currentKey を上書きしないよう playJob で二重確認する。
    val currentKey: KEY?
        get() = _currentKey.takeIf { playJob?.isActive == true }

    @Volatile
    private var _currentIsManual: Boolean = false

    // currentKey と同様に、再生中のイベントが手動読み上げ由来かを返す。再生中でなければ false。
    val currentIsManual: Boolean
        get() = _currentIsManual && playJob?.isActive == true

    // キューで再生待ちのイベント（再生順）。再生中のイベントは含まない。
    // 再生が終わった時点で次の待機イベントのキー・手動フラグへ公開状態を引き継ぐために使う。
    // commonMain では synchronized が使えないため、不変リストの差し替えは speak() / stop() を呼ぶ側だけが行う。
    // 再生側（play()）はリストを書き換えず、開始した項目に started を立てるだけにする。
    // 削除を再生側と呼び出し側で競合させると、終了済みの項目が復活して誤った公開状態を引き継ぐため。
    @Volatile
    private var pendingPlays: List<PendingPlay<KEY>> = emptyList()

    private data class PendingPlay<KEY>(
        val key: KEY,
        val manual: Boolean,
    ) {
        @Volatile
        var started: Boolean = false
    }

    private fun newPlaybackScope(): CoroutineScope = CoroutineScope(scope.coroutineContext + SupervisorJob())

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
        isManual: Boolean = false,
        playStartSound: Boolean = true,
    ) {
        val body = playbackBody(event) ?: return
        val manual = isManual || isManualEvent(event)
        if (queue) {
            // stop() 直後で playbackScope がキャンセル済みのままだと、その配下へ launch した
            // 瞬間に子ジョブごとキャンセルされてしまうため、生存中でなければ差し替える。
            if (!playbackScope.isActive) playbackScope = newPlaybackScope()
            val barrier = stopBarrier
            val previousJob = playJob
            val key = eventToKey(event)
            // 再生中も再生待ちもなければ、このイベントが次に再生される。非キュー経路と同様に今すぐ公開する。
            // 公開しないと play() に入るまで currentKey が null になり、その間に届いた別のイベントが
            // 「再生中なし」と判定されて、再生待ちのこのイベントをキャンセルしてしまう。
            // すでに再生中・再生待ちがあるときは、その公開状態が先に続くため待機リストへ積むだけにする。
            val pending =
                if (currentKey == null) {
                    _currentKey = key
                    _currentIsManual = manual
                    null
                } else {
                    PendingPlay(key, manual).also { pendingPlays = pendingPlays.filterNot { p -> p.started } + it }
                }
            playJob =
                playbackScope.launch {
                    barrier.join()
                    previousJob?.join()
                    pending?.started = true
                    play(event, manual, body, playStartSound)
                }
            return
        }
        val barrier = cancelPlayback()
        // 前の再生の停止待ち（barrier）の間も、この新しいイベントを再生中として公開する。
        // 公開しないと play() に入るまで currentKey が null になり、続けて届いた自動・手動の
        // イベントが「再生中なし」と判定されて、待機中のこのイベントをキャンセルしてしまう。
        _currentKey = eventToKey(event)
        _currentIsManual = manual
        playbackScope = newPlaybackScope()
        playJob =
            playbackScope.launch {
                barrier.join()
                play(event, manual, body, playStartSound)
            }
    }

    /**
     * [event] の本編（開始音の後に再生するもの）。[isCustomSpeakEvent] の対象は [customSpeak] による読み上げ。
     * 対象外のイベントは再生対象外として null を返す。引数は読み上げ音量（0〜100）。
     */
    private fun playbackBody(event: EVENT): (suspend (Int) -> Unit)? {
        if (!isCustomSpeakEvent(event)) return null
        return { volume -> customSpeak(event, volume) }
    }

    private suspend fun play(
        event: EVENT,
        manual: Boolean,
        body: suspend (Int) -> Unit,
        playStartSound: Boolean,
    ) {
        val key = eventToKey(event)
        _currentKey = key
        _currentIsManual = manual
        val vol = currentVolume
        val startSoundEnabled = currentStartSoundEnabledStates[key] ?: true
        if (playStartSound && startSoundEnabled) {
            startSounds[currentStartSoundType]?.let { soundPlayer.play(it, vol) }
        }
        body(vol)
        // キューに後続のジョブが待機している間は、再生中の状態を消さず、次の待機イベントの
        // キー・手動フラグへ引き継ぐ。消すと、次のジョブが play() に入るまでの間に届いたイベントが
        // 「再生中なし」と判定され、待機中のジョブをキャンセルする。
        // 引き継がないと、直前のイベントの手動フラグが次のイベントの優先度判定に残ってしまう。
        if (playJob === currentCoroutineContext().job) {
            _currentKey = null
            _currentIsManual = false
        } else {
            pendingPlays.firstOrNull { !it.started }?.let {
                _currentKey = it.key
                _currentIsManual = it.manual
            }
        }
    }

    fun stop() {
        cancelPlayback()
    }

    // playbackScope.cancel() は SoundPlayer の停止処理を非同期にトリガーするだけで、
    // 呼び出した時点では前の再生がまだ鳴っている。過去のバリアと今回キャンセルした親 Job の
    // 完了を順に待つバリアを返し、次の再生はそれを join() してから開始する。
    // バリアは playbackScope 配下ではなく scope へ直接 launch するため、連続割り込みで
    // 再生側の待機ジョブがキャンセルされても、過去の停止完了待ちは失われない。
    //
    // ここでは playbackScope を新しい Job に差し替えない。stop() 後もキャンセル済みの親を
    // 保持し、新しい再生を実際に launch する直前に差し替える。停止処理中の親を空の親へ
    // 置き換えず、stop() を含む各呼び出しの停止完了待ちを stopBarrier に連鎖させる。
    private fun cancelPlayback(): Job {
        val cancelled = playbackScope.coroutineContext.job
        cancelled.cancel()
        playJob = null
        // キャンセルされた play() は _currentKey のクリアまで進まないため、停止待ちで新しいジョブが
        // active になった間に古いキーが currentKey として見えないようここで消す。
        _currentKey = null
        _currentIsManual = false
        pendingPlays = emptyList()
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
        playbackScope = newPlaybackScope()
        playJob =
            playbackScope.launch {
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
        playbackScope = newPlaybackScope()
        val job =
            playbackScope.launch {
                barrier.join()
                soundPlayer.play(sound, currentVolume)
            }
        playJob = job
        try {
            job.join()
        } catch (e: CancellationException) {
            // 呼び出し元のキャンセルを再生ジョブへ伝え、開始音を止める。
            job.cancel()
            throw e
        }
    }
}
