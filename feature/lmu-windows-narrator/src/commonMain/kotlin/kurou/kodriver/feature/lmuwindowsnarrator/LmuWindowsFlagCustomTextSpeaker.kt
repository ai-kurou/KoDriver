package kurou.kodriver.feature.lmuwindowsnarrator

import kotlinx.coroutines.flow.first
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/**
 * フラッグの自由文字列をOS標準TTSで読み上げる、
 * [WavNarratorEngine][kurou.kodriver.core.narrator.WavNarratorEngine] 用のフック。
 * 全フラッグで過去の収録音声選択設定を無視する。
 * 空欄またはTTSが利用できない場合は収録済みWAVへフォールバックする。
 *
 * 対象イベントと文言の対応:
 * - [SpeechEvent.YellowFlag] : セクターイエロー
 * - [SpeechEvent.BlueFlag] : ブルー
 * - [SpeechEvent.FullCourseYellow] : フルコースイエロー
 * - [SpeechEvent.RedFlag] / [SpeechEvent.SessionStop] : レッド（2つの音声種別で1つの文言を共有する）
 */
internal class LmuWindowsFlagCustomTextSpeaker(
    private val observeSectorYellowFlagReadoutText: ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase,
    private val observeBlueFlagReadoutText: ObserveLmuWindowsBlueFlagReadoutTextUseCase,
    private val observeFullCourseYellowFlagReadoutText: ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase,
    private val observeRedFlagReadoutText: ObserveLmuWindowsRedFlagReadoutTextUseCase,
    private val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    private val speakText: SpeakTextUseCase,
) {
    /**
     * @param volume アプリの読み上げ音量（0〜100）。
     * @return カスタム文言を読み上げた場合 true（呼び出し元はWAVの再生をスキップする）。
     *   [event] がフラッグ以外、カスタム文言が未設定、またはTTSが利用不可の場合は
     *   false（WAVでの読み上げに任せる）。
     */
    suspend operator fun invoke(
        event: SpeechEvent,
        volume: Int,
    ): Boolean {
        val text = customText(event) ?: return false
        if (text.isBlank()) return false
        if (!checkTextToSpeechAvailable()) return false
        speakText(text, volume = volume)
        return true
    }

    private suspend fun customText(event: SpeechEvent): String? =
        when (event) {
            SpeechEvent.YellowFlag -> observeSectorYellowFlagReadoutText().first()
            SpeechEvent.BlueFlag -> observeBlueFlagReadoutText().first()
            SpeechEvent.FullCourseYellow -> observeFullCourseYellowFlagReadoutText().first()
            SpeechEvent.RedFlag, SpeechEvent.SessionStop -> observeRedFlagReadoutText().first()
            else -> null
        }
}
