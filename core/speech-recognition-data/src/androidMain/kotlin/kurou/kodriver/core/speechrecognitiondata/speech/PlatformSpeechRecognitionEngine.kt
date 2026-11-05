package kurou.kodriver.core.speechrecognitiondata.speech

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Android の [SpeechRecognizer] を使う [SpeechRecognitionEngine]。メインスレッドで生成・操作する。
 * [preferOnDevice] が true で端末内認識が使える場合はそれを使い、使えなければ通常の認識サービスを使う。
 * プラットフォームのAPIを直接呼ぶ薄いアダプターのため、単体テストの対象にしない。
 */
internal class PlatformSpeechRecognitionEngine(
    private val context: Context,
    preferOnDevice: Boolean,
) : SpeechRecognitionEngine {
    private var recognizer: SpeechRecognizer? = null

    override val isOnDevice: Boolean =
        preferOnDevice &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)

    override fun start(listener: SpeechRecognitionEngineListener) {
        val created = createRecognizer()
        recognizer = created
        created.setRecognitionListener(PlatformListener(listener))
        created.startListening(createIntent())
    }

    override fun stop() {
        recognizer?.destroy()
        recognizer = null
    }

    private fun createRecognizer(): SpeechRecognizer =
        if (isOnDevice) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }

    private fun createIntent(): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, RECOGNITION_LANGUAGE_TAG)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MAX_RESULTS)
        }

    private class PlatformListener(
        private val listener: SpeechRecognitionEngineListener,
    ) : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = listener.onReady()

        override fun onPartialResults(partialResults: Bundle?) {
            partialResults.candidates().firstOrNull()?.let(listener::onPartial)
        }

        override fun onResults(results: Bundle?) = listener.onResults(results.candidates())

        override fun onError(error: Int) = listener.onError(error)

        override fun onBeginningOfSpeech() = Unit

        override fun onRmsChanged(rmsdB: Float) = listener.onLevel(normalizeRecognitionRmsDb(rmsdB))

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() = Unit

        override fun onEvent(
            eventType: Int,
            params: Bundle?,
        ) = Unit

        private fun Bundle?.candidates(): List<String> =
            this?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
    }

    private companion object {
        const val RECOGNITION_LANGUAGE_TAG = "ja-JP"
        const val MAX_RESULTS = 5
    }
}
