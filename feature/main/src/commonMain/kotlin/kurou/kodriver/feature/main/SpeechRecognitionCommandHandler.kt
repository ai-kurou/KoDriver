package kurou.kodriver.feature.main

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kurou.kodriver.domain.acewindows.model.matchAceWindowsManualReadoutPhrase
import kurou.kodriver.domain.captureErrorTypeOnly
import kurou.kodriver.domain.gt7ps5.model.matchGt7Ps5ManualReadoutPhrase
import kurou.kodriver.domain.lmuwindows.model.matchLmuWindowsManualReadoutPhrase
import kurou.kodriver.domain.model.ManualReadoutItemRequest
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.containsSpeechRecognitionWakeWord
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.RequestManualReadoutItemUseCase

/** 認識した候補を、ウェイクワード待ち・コマンド待ちの状態に応じて、ウェイクワードの検出か読み上げ項目の依頼に振り分ける。 */
internal class SpeechRecognitionCommandHandler(
    private val requestManualReadoutItem: RequestManualReadoutItemUseCase,
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    private val commandWait: CommandWaitController,
) {
    suspend fun onRecognizedCandidates(candidates: List<String>) {
        when {
            commandWait.paused -> {}

            !commandWait.commandWaiting -> {
                if (containsSpeechRecognitionWakeWord(candidates)) commandWait.startCommandWait()
            }

            else -> {
                try {
                    if (requestManualReadoutItem.requestMatchedReadouts(
                            observeSelectedSimulator().first(),
                            candidates,
                        )
                    ) {
                        commandWait.finishCommandWait()
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // 1回の依頼に失敗しても聞き取り全体は止めない。種類だけ記録してウェイクワード待ちへ戻る
                    captureErrorTypeOnly(e)
                    commandWait.finishCommandWait()
                }
            }
        }
    }
}

/**
 * 選択中のシミュレーターの語句だけを照合し、一致したら依頼を発行する。依頼したら true。
 * 選択中でないシミュレーターの語句は照合しない（そのNarratorは依頼を無視するため、コマンド待ちだけが終わってしまう）。
 * 音声認識の読み上げでは開始音を鳴らさない（playStartSound = false）。
 */
private suspend fun RequestManualReadoutItemUseCase.requestMatchedReadouts(
    selected: Simulator,
    candidates: List<String>,
): Boolean {
    val match =
        when (selected) {
            Simulator.LmuWindows -> matchLmuWindowsManualReadoutPhrase(candidates)
            Simulator.Gt7Ps5 -> matchGt7Ps5ManualReadoutPhrase(candidates)
            Simulator.AceWindows -> matchAceWindowsManualReadoutPhrase(candidates)
        } ?: return false
    this(ManualReadoutItemRequest(selected.id, match.itemKey, playStartSound = false, matchedPhrase = match.phrase))
    return true
}
