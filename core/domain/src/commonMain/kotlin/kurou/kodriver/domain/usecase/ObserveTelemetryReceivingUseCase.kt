package kurou.kodriver.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TELEMETRY_RECEIVING_TIMEOUT_MS_DEFAULT

/**
 * 選択中シミュレータのテレメトリを実際に受信中かどうかを監視する UseCase。
 *
 * 接続バナーの CONNECTED 状態（共有メモリのプローブ可否やサーバーバージョン取得の成否で判定）とは異なり、
 * 各シミュレータの生テレメトリ Flow が直近 [TELEMETRY_RECEIVING_TIMEOUT_MS_DEFAULT] 以内に
 * データを流しているかどうかで判定する。テレメトリを受信するたびに [transformLatest] がタイムアウト待機を
 * 最初からやり直すため、ticker によるポーリングを使わずに正確なタイムアウトを実現する。
 */
class ObserveTelemetryReceivingUseCase(
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    private val observeLmuWindows: ObserveLmuWindowsUseCase,
    private val observeGt7Ps5: ObserveGt7Ps5UseCase,
    private val observeAceWindowsStatus: ObserveAceWindowsStatusUseCase,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Boolean> =
        observeSelectedSimulator()
            .flatMapLatest { simulator -> telemetryReceivingFlow(simulator) }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun telemetryReceivingFlow(simulator: Simulator): Flow<Boolean> =
        telemetryStream(simulator)
            .transformLatest {
                emit(true)
                delay(TELEMETRY_RECEIVING_TIMEOUT_MS_DEFAULT)
                emit(false)
            }.onStart { emit(false) }

    private fun telemetryStream(simulator: Simulator): Flow<*> =
        when (simulator) {
            is Simulator.LmuWindows -> observeLmuWindows()
            is Simulator.Gt7Ps5 -> observeGt7Ps5()
            is Simulator.AceWindows -> observeAceWindowsStatus()
        }
}
