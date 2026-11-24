package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kurou.kodriver.domain.captureError
import kurou.kodriver.domain.captureErrorTypeOnly
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kurou.kodriver.domain.model.SpeechRecognitionMode
import kotlin.coroutines.cancellation.CancellationException

/**
 * 音声の断片 1 つ分を [SpeechTranscriber] で認識して [SpeechRecognitionEvent] にする、Desktop・Android 共通の処理。
 *
 * 音量（[SpeechRecognitionEvent.Level]）を流し、[mode] の変化を [SpeechTranscriber] へ反映し、
 * 認識結果を [SpeechRecognitionEvent.Partial] / [SpeechRecognitionEvent.Recognized] にする。
 * 同じ途中経過は続けて流さず、モードが変わったときは途中経過を引き継がない。
 * 状態を持つため、1 回の聞き取りごとに作る。ただし [modeSync] は [SpeechTranscriber] が反映済みのモードを覚えているため、
 * 同じ [SpeechTranscriber] を使い続ける間は聞き取りをまたいで同じものを渡す。
 *
 * 音量ゲートの閾値（[pcmLevel] の 0.0〜1.0）は [volumeGateThreshold] の現在値で、これ未満の断片は無音として認識器に渡し、
 * 小さい音による誤反応を防ぐ。ゲートはウェイクワード検出中に適用し、コマンド受付中などは [cutQuietSoundInCommand] が true のときだけ適用する。0.0 ならゲートは常に開く。設定の変更は次の断片から反映される。
 */
internal class RecognitionPipeline(
    private val transcriber: SpeechTranscriber,
    private val modeSync: TranscriberModeSync,
    private val volumeGateThreshold: StateFlow<Float>,
    private val cutQuietSoundInCommand: StateFlow<Boolean>,
) {
    constructor(
        transcriber: SpeechTranscriber,
        mode: StateFlow<SpeechRecognitionMode>,
        volumeGateThreshold: StateFlow<Float>,
        cutQuietSoundInCommand: StateFlow<Boolean>,
    ) : this(transcriber, TranscriberModeSync(transcriber, mode), volumeGateThreshold, cutQuietSoundInCommand)

    private var lastPartial = ""
    private var lastMode = SpeechRecognitionMode.WakeWord
    private var volumeGateHoldChunks = 0

    suspend fun FlowCollector<SpeechRecognitionEvent>.process(pcm: ByteArray) {
        val level = pcm.pcmLevel()
        emit(SpeechRecognitionEvent.Level(level))
        val previousMode = lastMode
        lastMode = modeSync.sync()
        if (lastMode != previousMode) lastPartial = ""
        val input = if (isVolumeGateOpen(level, lastMode)) pcm else ByteArray(pcm.size)
        when (val transcript = transcriber.accept(input)) {
            is SpeechTranscript.Partial -> {
                if (transcript.text != lastPartial) {
                    lastPartial = transcript.text
                    emit(SpeechRecognitionEvent.Partial(transcript.text))
                }
            }

            is SpeechTranscript.Final -> {
                lastPartial = ""
                emit(SpeechRecognitionEvent.Recognized(transcript.candidates))
            }

            null -> {}
        }
    }

    /**
     * 音量ゲートの開閉を決める。ゲートはウェイクワード検出中（[SpeechRecognitionMode.WakeWord]）と、
     * [cutQuietSoundInCommand] が true のときのコマンド受付中（[SpeechRecognitionMode.Command]）に適用する。
     * 適用しないモードでは常に開き、ホールドも捨てる。
     * 閾値以上の断片でホールドを [VOLUME_GATE_HOLD_CHUNKS] に戻し、
     * 閾値未満の断片はホールドが残っている間だけ開のまま、残数を1つ減らす。
     */
    private fun isVolumeGateApplied(mode: SpeechRecognitionMode): Boolean =
        when (mode) {
            SpeechRecognitionMode.WakeWord -> true
            SpeechRecognitionMode.Command -> cutQuietSoundInCommand.value
            SpeechRecognitionMode.Paused -> false
        }

    private fun isVolumeGateOpen(
        level: Float,
        mode: SpeechRecognitionMode,
    ): Boolean {
        if (!isVolumeGateApplied(mode)) {
            volumeGateHoldChunks = 0
            return true
        }
        if (level >= volumeGateThreshold.value) {
            volumeGateHoldChunks = VOLUME_GATE_HOLD_CHUNKS
            return true
        }
        val open = volumeGateHoldChunks > 0
        if (open) volumeGateHoldChunks--
        return open
    }
}

/**
 * 音量ゲートが閾値以上の断片のあと開いたままにする断片数。断片は100ms（500ms分）。
 * 語尾や息継ぎの小さい音を確定まで認識器に渡すための既定値であり、実機で調整する前提。
 */
internal const val VOLUME_GATE_HOLD_CHUNKS = 5

/**
 * 認識モデルが使えれば [SpeechTranscriber] を作る。使えない・読み込めないときは記録して null を返す。
 */
internal fun SpeechTranscriberFactory.createOrNull(): SpeechTranscriber? {
    if (!isAvailable()) {
        captureError(IllegalStateException("音声認識を利用できません。認識モデルが見つかりません"))
        return null
    }
    return try {
        create()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        captureError(e)
        null
    } catch (e: LinkageError) {
        // 対応していないOS向けのネイティブライブラリが読み込めない場合。初回失敗後のクラス初期化エラー（NoClassDefFoundError）も含む
        captureError(e)
        null
    }
}

/** 認識中に想定外の例外が出ても、続行できないことを `Failed(RecognitionFailed)` で知らせて完了する。 */
internal fun Flow<SpeechRecognitionEvent>.failRecognitionOnError(): Flow<SpeechRecognitionEvent> =
    catch { e ->
        if (e !is Exception && e !is LinkageError) throw e
        captureErrorTypeOnly(e)
        emit(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.RecognitionFailed))
    }
