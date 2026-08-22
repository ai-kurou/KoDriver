package kurou.kodriver.domain.usecase

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kurou.kodriver.domain.model.CONNECTION_CHECK_INTERVAL_MS_DEFAULT
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData

data class Gt7Ps5ConnectionState(
    val isConnected: Boolean,
    val telemetry: Gt7Ps5TelemetryData? = null,
)

class ObserveGt7Ps5ConnectionUseCase(
    private val checkGt7Ps5Connection: CheckGt7Ps5ConnectionUseCase,
    private val observeGt7Ps5: ObserveGt7Ps5UseCase,
) {
    operator fun invoke(): Flow<Gt7Ps5ConnectionState> =
        connectionCheckFlow().combine(
            observeGt7Ps5()
                .map<Gt7Ps5TelemetryData, Gt7Ps5TelemetryData?> { telemetry -> telemetry }
                .onStart { emit(null) },
        ) { isConnected, telemetry ->
            Gt7Ps5ConnectionState(
                isConnected = isConnected,
                telemetry = telemetry,
            )
        }

    private fun connectionCheckFlow() =
        flow {
            while (true) {
                val isConnected =
                    try {
                        checkGt7Ps5Connection()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        false
                    }
                emit(isConnected)
                delay(CONNECTION_CHECK_INTERVAL_MS_DEFAULT)
            }
        }
}
