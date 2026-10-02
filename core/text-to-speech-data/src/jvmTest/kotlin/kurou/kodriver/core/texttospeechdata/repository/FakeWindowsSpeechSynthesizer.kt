package kurou.kodriver.core.texttospeechdata.repository

import kurou.kodriver.core.texttospeechdata.windows.WindowsSpeechSynthesizer
import kurou.kodriver.domain.model.TextToSpeechVoice
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * @param blockUntilInterrupted `true` の場合、[speak] は割り込み（[Thread.interrupt]）が来るまで
 *   ブロックし続ける。[WindowsTextToSpeechRepository.speak] がコルーチンのキャンセル時に
 *   実際にブロック中のスレッドへ割り込みを送る（`runInterruptible`）ことを検証するために使う。
 */
class FakeWindowsSpeechSynthesizer(
    private val available: Boolean = true,
    private val blockUntilInterrupted: Boolean = false,
    var voices: List<TextToSpeechVoice> = emptyList(),
    private val listVoicesRelease: CountDownLatch? = null,
) : WindowsSpeechSynthesizer {
    val spokenTexts = mutableListOf<Pair<String, Boolean>>()
    val spokenVolumes = mutableListOf<Int>()
    val spokenVoiceIds = mutableListOf<String>()
    var stopCount = 0
        private set
    var wasInterrupted = false
        private set

    /** [speak] が呼ばれてブロックを開始したことを、呼び出し側スレッドから待ち合わせるためのラッチ。 */
    val speakStarted = CountDownLatch(1)

    var isAvailableCallCount = 0
        private set

    var listVoicesCallCount = 0
        private set
    var listVoicesThread: Thread? = null
        private set
    val listVoicesStarted = CountDownLatch(1)

    override fun listVoices(): List<TextToSpeechVoice> {
        listVoicesCallCount++
        listVoicesThread = Thread.currentThread()
        listVoicesStarted.countDown()
        check(listVoicesRelease?.await(5, TimeUnit.SECONDS) != false)
        return voices
    }

    override fun isAvailable(): Boolean {
        isAvailableCallCount++
        return available
    }

    override fun speak(
        text: String,
        queue: Boolean,
        volume: Int,
        voiceId: String,
    ) {
        spokenTexts += text to queue
        spokenVolumes += volume
        spokenVoiceIds += voiceId
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
