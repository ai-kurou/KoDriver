package kurou.kodriver.feature.acewindowsnarrator

import kotlinx.coroutines.flow.first
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/**
 * ACE の保存した自由文言をOS標準TTSで読み上げる。空白・TTS利用不可なら読み上げない。
 * 対象イベント: [SpeechEvent.AceWindowsCheckeredFlag]（チェッカーフラッグ）。
 */
internal class AceWindowsReadoutTextSpeaker(
    private val observeCheckeredFlagReadoutText: ObserveAceWindowsCheckeredFlagReadoutTextUseCase,
    private val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    private val speakText: SpeakTextUseCase,
) {
    suspend operator fun invoke(
        event: SpeechEvent,
        volume: Int,
    ) {
        val text = readoutText(event) ?: return
        speakText(text, volume = volume)
    }

    /** 現在の読み上げ文言。空白・TTS利用不可・対象外イベントは null。 */
    suspend fun readoutText(event: SpeechEvent): String? {
        val text =
            when (event) {
                SpeechEvent.AceWindowsCheckeredFlag -> observeCheckeredFlagReadoutText().first()
                else -> return null
            }
        return text.takeIf { it.isNotBlank() && checkTextToSpeechAvailable() }
    }
}

/** 自由文言対象の判定をNarratorと本文再生で共有する。後続フラッグはここへ追加する。 */
internal fun isAceWindowsCustomSpeakEvent(event: SpeechEvent): Boolean =
    when (event) {
        SpeechEvent.AceWindowsCheckeredFlag -> true
        else -> false
    }
