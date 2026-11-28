package kurou.kodriver.domain.speechrecognition.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * 音声認識の ON/OFF を、画面（feature）間で共有するRepository。
 * 聞き取りを行う側が [update] で通知し、他の画面が [enabled] を購読する。
 */
public interface SpeechRecognitionEnabledRepository {
    /** 音声認識が ON か。通知されるまでは OFF。 */
    public val enabled: StateFlow<Boolean>

    /** 現在の ON/OFF を [enabled] に反映する。 */
    public fun update(enabled: Boolean)
}
