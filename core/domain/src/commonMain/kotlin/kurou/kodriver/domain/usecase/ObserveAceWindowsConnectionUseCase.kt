package kurou.kodriver.domain.usecase

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.CONNECTION_CHECK_INTERVAL_MS_DEFAULT

data class AceWindowsConnectionState(
    val isConnected: Boolean,
    val fuel: AceWindowsFuelData? = null,
)

class ObserveAceWindowsConnectionUseCase(
    private val checkAceWindowsConnection: CheckAceWindowsConnectionUseCase,
    private val observeAceWindowsFuel: ObserveAceWindowsFuelUseCase,
) {
    operator fun invoke(): Flow<AceWindowsConnectionState> =
        connectionCheckFlow().combine(
            observeAceWindowsFuel()
                .map<AceWindowsFuelData, AceWindowsFuelData?> { fuel -> fuel }
                .onStart { emit(null) },
        ) { isConnected, fuel ->
            AceWindowsConnectionState(
                isConnected = isConnected,
                fuel = fuel,
            )
        }

    private fun connectionCheckFlow() =
        flow {
            while (true) {
                val isConnected =
                    try {
                        checkAceWindowsConnection()
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
