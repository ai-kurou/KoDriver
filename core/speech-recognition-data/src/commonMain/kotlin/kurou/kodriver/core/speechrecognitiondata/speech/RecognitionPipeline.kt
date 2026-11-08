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
 * 状態を持つため、1 回の聞き取りごとに作る。
 */
internal class RecognitionPipeline(
    private val transcriber: SpeechTranscriber,
    mode: StateFlow<SpeechRecognitionMode>,
) {
    private val modeSync = TranscriberModeSync(transcriber, mode)
    private var lastPartial = ""
    private var lastMode = SpeechRecognitionMode.WakeWord

    suspend fun FlowCollector<SpeechRecognitionEvent>.process(pcm: ByteArray) {
        emit(SpeechRecognitionEvent.Level(pcm.pcmLevel()))
        val previousMode = lastMode
        lastMode = modeSync.sync()
        if (lastMode != previousMode) lastPartial = ""
        when (val transcript = transcriber.accept(pcm)) {
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
}

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
