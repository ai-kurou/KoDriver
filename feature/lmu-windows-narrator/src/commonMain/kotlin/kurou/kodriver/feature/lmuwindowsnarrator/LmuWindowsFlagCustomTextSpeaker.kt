package kurou.kodriver.feature.lmuwindowsnarrator

import kotlinx.coroutines.flow.first
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.LmuWindowsFlagReadoutTarget
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagRecordedVoiceSelectedUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/**
 * フラッグが実際に発生した際、収録音声（チップ選択）とカスタム文言（TextField入力）の
 * どちらで読み上げるかを切り替える [WavNarratorEngine][kurou.kodriver.core.narrator.WavNarratorEngine]
 * 用のフック。detailPane の試聴と同じく、カスタム文言が設定されていて、かつOS標準TTSが実際に
 * 利用可能な場合のみOS標準TTSで読み上げる。空欄（未設定）、収録音声が明示的に選ばれている場合
 * （文言は残っていても使わない）、または現在TTSが利用できない場合は、
 * 収録済みWAVでの読み上げに任せる（TTSが後から使えなくなっても無音・意図しない言語での読み上げに
 * ならないようにするためのフォールバック）。
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
    private val observeRecordedVoiceSelected: ObserveLmuWindowsFlagRecordedVoiceSelectedUseCase,
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
        // ブルー・イエローフラッグはチップを持たないため、過去に保存された選択状態は無視する。
        if (event != SpeechEvent.BlueFlag && event != SpeechEvent.YellowFlag &&
            observeRecordedVoiceSelected(target(event)).first()
        ) {
            return false
        }
        if (!checkTextToSpeechAvailable()) return false
        speakText(text, volume = volume)
        return true
    }

    private fun target(event: SpeechEvent): LmuWindowsFlagReadoutTarget =
        when (event) {
            SpeechEvent.YellowFlag -> LmuWindowsFlagReadoutTarget.SECTOR_YELLOW_FLAG
            SpeechEvent.BlueFlag -> LmuWindowsFlagReadoutTarget.BLUE_FLAG
            SpeechEvent.FullCourseYellow -> LmuWindowsFlagReadoutTarget.FULL_COURSE_YELLOW
            else -> LmuWindowsFlagReadoutTarget.RED_FLAG
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
