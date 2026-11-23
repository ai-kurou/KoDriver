package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.StateFlow
import kurou.kodriver.domain.model.SpeechRecognitionTranscript

/**
 * マイクが聞き取った文言を、画面（feature）間で共有するRepository。
 * 聞き取りを行う側が [update] で通知し、オーバーレイなど他の画面が [transcript] を購読して表示する。
 * 認識エンジンの聞き取りを購読し直さずに済むよう（マイクを二重に開かないよう）、結果だけを共有する。
 */
public interface SpeechRecognitionTranscriptRepository {
    /** 現在の聞き取り状況。購読を始めた時点の値もすぐに返る。 */
    public val transcript: StateFlow<SpeechRecognitionTranscript>

    /** 現在の聞き取り状況を [transcript] に置き換える。 */
    public fun update(transcript: SpeechRecognitionTranscript)
}
