package kurou.kodriver.domain.usecase

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ObserveLmuWindowsConnectionUseCase(
    private val checkLmuWindowsConnection: CheckLmuWindowsConnectionUseCase,
) {
    operator fun invoke(): Flow<Boolean> =
        flow {
            while (true) {
                val isConnected =
                    try {
                        checkLmuWindowsConnection()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        false
                    }
                emit(isConnected)
                delay(CONNECTION_CHECK_INTERVAL_MS)
            }
        }

    private companion object {
        const val CONNECTION_CHECK_INTERVAL_MS = 1_000L
    }
}
