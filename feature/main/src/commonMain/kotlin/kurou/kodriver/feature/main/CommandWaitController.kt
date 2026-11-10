package kurou.kodriver.feature.main

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kurou.kodriver.domain.captureErrorTypeOnly
import kurou.kodriver.domain.launchCapturingErrors
import kurou.kodriver.domain.model.SpeechRecognitionChime
import kurou.kodriver.domain.model.SpeechRecognitionMode

/**
 * 聞き取りのモード（ウェイクワード待ち・コマンド待ち・読み上げ中の停止）と、それに付随するコマンド待ちの時間切れ・確認音を管理する。
 *
 * [setMode] へ今の状態に合うモードを伝える。コマンド待ちの開始・終了で画面の表示を変える処理は
 * [onCommandWaitStarted] / [onCommandWaitEnded] で呼び出し側に任せる。
 */
internal class CommandWaitController(
    private val scope: CoroutineScope,
    private val setMode: (SpeechRecognitionMode) -> Unit,
    private val playChime: suspend (SpeechRecognitionChime) -> Unit,
    private val onCommandWaitStarted: () -> Unit,
    private val onCommandWaitEnded: () -> Unit,
) {
    /** ウェイクワードの検出から [SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS] 経つと、ウェイクワード待ちへ戻す。 */
    private var commandTimeoutJob: Job? = null

    /** ウェイクワードの検出やコマンド待ちの時間切れを知らせる確認音。コマンド待ちを遅らせないよう、別のJobで鳴らす。 */
    private var chimeJob: Job? = null

    /** ウェイクワードを検出し、コマンドを待っているか。 */
    var commandWaiting = false
        private set

    /** 読み上げ中、または読み上げ終了後の再開待ちか。 */
    var paused = false
        private set

    /** 今の状態に合うモードをRepositoryへ伝える。 */
    fun applyMode() {
        setMode(
            when {
                paused -> SpeechRecognitionMode.Paused
                commandWaiting -> SpeechRecognitionMode.Command
                else -> SpeechRecognitionMode.WakeWord
            },
        )
    }

    /** ウェイクワードを検出した。確認音を鳴らしてコマンドを待ち始め、時間切れでウェイクワード待ちへ戻す。 */
    fun startCommandWait() {
        commandWaiting = true
        applyMode()
        launchChime(SpeechRecognitionChime.WakeWordDetected)
        onCommandWaitStarted()
        commandTimeoutJob?.cancel()
        commandTimeoutJob =
            scope.launchCapturingErrors {
                delay(SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS)
                finishCommandWait(timedOut = true)
            }
    }

    /** コマンド待ちを終えてウェイクワード待ちへ戻す。[timedOut] なら、通じなかったことを下降音で知らせる。 */
    fun finishCommandWait(timedOut: Boolean = false) {
        cancelWaitJobs()
        commandWaiting = false
        applyMode()
        onCommandWaitEnded()
        if (timedOut) launchChime(SpeechRecognitionChime.CommandTimedOut)
    }

    /** 読み上げが始まった。コマンド待ちは打ち切り、聞き取りを止める。確認音が読み上げに重ならないよう、鳴っていれば止める。 */
    fun pause() {
        cancelWaitJobs()
        val wasWaiting = commandWaiting
        commandWaiting = false
        paused = true
        applyMode()
        if (wasWaiting) onCommandWaitEnded()
    }

    /** 読み上げが終わって残響も消えた。聞き取りを再開する。 */
    fun resume() {
        paused = false
        applyMode()
    }

    /** 聞き取りの停止や失敗で、コマンド待ち・読み上げ中の状態と付随するJobを破棄する。モードは伝えない。 */
    fun reset() {
        cancelWaitJobs()
        commandWaiting = false
        paused = false
    }

    private fun cancelWaitJobs() {
        commandTimeoutJob?.cancel()
        commandTimeoutJob = null
        chimeJob?.cancel()
        chimeJob = null
    }

    /** 確認音を別のJobで鳴らす。鳴らせなくてもコマンド待ちは続けるため、失敗は種類だけ記録して握りつぶす。 */
    private fun launchChime(chime: SpeechRecognitionChime) {
        chimeJob?.cancel()
        chimeJob =
            scope.launch {
                try {
                    playChime(chime)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    captureErrorTypeOnly(e)
                }
            }
    }
}
