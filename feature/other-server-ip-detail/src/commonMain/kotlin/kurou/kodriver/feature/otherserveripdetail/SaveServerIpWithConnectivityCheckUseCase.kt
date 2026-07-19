package kurou.kodriver.feature.otherserveripdetail

import kotlinx.coroutines.CancellationException
import kurou.kodriver.domain.usecase.SaveServerIpUseCase

internal sealed interface SaveServerIpResult {
    data object Saved : SaveServerIpResult
    data object InvalidIp : SaveServerIpResult
    data object Unreachable : SaveServerIpResult
    data object SaveFailed : SaveServerIpResult
}

internal class SaveServerIpWithConnectivityCheckUseCase(
    private val validateServerIpAddress: ValidateServerIpAddressUseCase,
    private val connectivityChecker: ServerConnectivityChecker,
    private val saveServerIp: SaveServerIpUseCase,
) {
    suspend operator fun invoke(
        ip: String,
        checkConnectivity: Boolean = true,
    ): SaveServerIpResult {
        if (!validateServerIpAddress(ip)) return SaveServerIpResult.InvalidIp
        if (checkConnectivity && !connectivityChecker.isReachable(ip)) return SaveServerIpResult.Unreachable

        return try {
            saveServerIp(ip)
            SaveServerIpResult.Saved
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            SaveServerIpResult.SaveFailed
        }
    }
}
