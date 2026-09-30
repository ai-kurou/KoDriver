package kurou.kodriver.core.texttospeechdata.repository

import kurou.kodriver.core.texttospeechdata.windows.WindowsSpeechSynthesizer
import java.util.concurrent.CountDownLatch

/**
 * @param blockUntilInterrupted `true` の場合、[speak] は割り込み（[Thread.interrupt]）が来るまで
 *   ブロックし続ける。[WindowsTextToSpeechRepository.speak] がコルーチンのキャンセル時に
 *   実際にブロック中のスレッドへ割り込みを送る（`runInterruptible`）ことを検証するために使う。
 */
class FakeWindowsSpeechSynthesizer(
    private val available: Boolean = true,
    private val blockUntilInterrupted: Boolean = false,
) : WindowsSpeechSynthesizer {
    val spokenTexts = mutableListOf<Pair<String, Boolean>>()
    var stopCount = 0
        private set
    var wasInterrupted = false
        private set

    /** [speak] が呼ばれてブロックを開始したことを、呼び出し側スレッドから待ち合わせるためのラッチ。 */
    val speakStarted = CountDownLatch(1)

    var isAvailableCallCount = 0
        private set

    override fun isAvailable(): Boolean {
        isAvailableCallCount++
        return available
    }

    override fun speak(
        text: String,
        queue: Boolean,
    ) {
        spokenTexts += text to queue
        if (!blockUntilInterrupted) return
        speakStarted.countDown()
        try {
            Thread.sleep(Long.MAX_VALUE)
        } catch (e: InterruptedException) {
            wasInterrupted = true
            throw e
        }
    }

    override fun stop() {
        stopCount++
    }
}
