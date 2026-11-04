@file:Suppress("FunctionNaming")

package kurou.kodriver.data.speech

import kurou.kodriver.domain.repository.RecordAudioPermissionRepository

internal class FakeRecordAudioPermissionRepository(
    var granted: Boolean = true,
) : RecordAudioPermissionRepository {
    override fun isGranted(): Boolean = granted
}

/** 開始時に [script] を実行して、セッション内の通知を再現する。 */
internal class FakeSpeechRecognitionEngine(
    override val isOnDevice: Boolean = false,
    private val startFailure: Exception? = null,
    private val script: (SpeechRecognitionEngineListener) -> Unit = {},
) : SpeechRecognitionEngine {
    var startCount = 0
    var stopCount = 0

    override fun start(listener: SpeechRecognitionEngineListener) {
        startCount++
        startFailure?.let { throw it }
        script(listener)
    }

    override fun stop() {
        stopCount++
    }
}
