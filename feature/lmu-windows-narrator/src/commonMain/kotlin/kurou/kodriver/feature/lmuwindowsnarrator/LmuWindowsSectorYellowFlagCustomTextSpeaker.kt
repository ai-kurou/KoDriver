package kurou.kodriver.feature.lmuwindowsnarrator

import kotlinx.coroutines.flow.first
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/**
 * イエローフラッグが実際に発生した際、収録音声（チップ選択）とカスタム文言（TextField入力）の
 * どちらで読み上げるかを切り替える [WavNarratorEngine][kurou.kodriver.core.narrator.WavNarratorEngine]
 * 用のフック。detailPane の試聴（
 * [kurou.kodriver.feature.lmuwindowsreadout.flagdetail.LmuWindowsReadoutFlagDetailViewModel] の
 * onSectorYellowFlagTextPreviewClicked）と同じく、カスタム文言が設定されていればOS標準TTSで読み上げ、
 * 空欄（未設定）なら収録済みWAVでの読み上げに任せる。
 */
internal class LmuWindowsSectorYellowFlagCustomTextSpeaker(
    private val observeSectorYellowFlagReadoutText: ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase,
    private val speakText: SpeakTextUseCase,
) {
    /**
     * @return カスタム文言を読み上げた場合 true（呼び出し元はWAVの再生をスキップする）。
     *   [event] がイエローフラッグ以外、またはカスタム文言が未設定の場合は false（WAVでの読み上げに任せる）。
     */
    suspend operator fun invoke(event: SpeechEvent): Boolean {
        if (event != SpeechEvent.YellowFlag) return false
        val text = observeSectorYellowFlagReadoutText().first()
        if (text.isBlank()) return false
        speakText(text)
        return true
    }
}
