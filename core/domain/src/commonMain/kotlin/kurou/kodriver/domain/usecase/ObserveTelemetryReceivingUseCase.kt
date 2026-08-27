package kurou.kodriver.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kurou.kodriver.domain.model.CONNECTION_CHECK_INTERVAL_MS_DEFAULT
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TELEMETRY_RECEIVING_TIMEOUT_MS_DEFAULT
import kotlin.time.Clock

/**
 * 選択中シミュレータのテレメトリを実際に受信中かどうかを監視する UseCase。
 *
 * 接続バナーの CONNECTED 状態（共有メモリのプローブ可否やサーバーバージョン取得の成否で判定）とは異なり、
 * 各シミュレータの生テレメトリ Flow が直近 [TELEMETRY_RECEIVING_TIMEOUT_MS_DEFAULT] 以内に
 * データを流しているかどうかで判定する。
 */
class ObserveTelemetryReceivingUseCase(
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    private val observeLmuWindows: ObserveLmuWindowsUseCase,
    private val observeGt7Ps5: ObserveGt7Ps5UseCase,
    private val observeAceWindowsStatus: ObserveAceWindowsStatusUseCase,
    private val currentTimeMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Boolean> =
        observeSelectedSimulator()
            .flatMapLatest { simulator -> telemetryReceivingFlow(simulator) }

    private fun telemetryReceivingFlow(simulator: Simulator): Flow<Boolean> {
        var lastReceivedAtMillis: Long? = null

        val onTelemetryReceived =
            telemetryStream(simulator).map { lastReceivedAtMillis = currentTimeMillis() }

        return merge(onTelemetryReceived, tickerFlow())
            .map { isReceivingRecently(lastReceivedAtMillis) }
            .onStart { emit(false) }
    }

    private fun telemetryStream(simulator: Simulator): Flow<*> =
        when (simulator) {
            is Simulator.LmuWindows -> observeLmuWindows()
            is Simulator.Gt7Ps5 -> observeGt7Ps5()
            is Simulator.AceWindows -> observeAceWindowsStatus()
        }

    private fun isReceivingRecently(lastReceivedAtMillis: Long?): Boolean =
        lastReceivedAtMillis != null &&
            currentTimeMillis() - lastReceivedAtMillis < TELEMETRY_RECEIVING_TIMEOUT_MS_DEFAULT

    private fun tickerFlow(): Flow<Unit> =
        flow {
            while (true) {
                delay(CONNECTION_CHECK_INTERVAL_MS_DEFAULT)
                emit(Unit)
            }
        }
}
