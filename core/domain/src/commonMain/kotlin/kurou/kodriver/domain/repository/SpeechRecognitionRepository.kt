package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.SpeechRecognitionEvent

/**
 * マイクからの常時聞き取り（音声認識）を行うRepository。
 */
interface SpeechRecognitionRepository {
    /** 端末で音声認識を利用できるか。音声認識を提供しないプラットフォームでは常に false。 */
    fun isAvailable(): Boolean

    /**
     * 常時聞き取りを開始し、認識イベントを流す cold Flow。収集を開始すると聞き取りを始め、
     * 収集のキャンセルで止める。無音・認識不能などの一時的な失敗は内部で再開する。
     * 続行できない場合は [SpeechRecognitionEvent.Failed] を流して完了する。
     */
    fun observeRecognition(): Flow<SpeechRecognitionEvent>
}
