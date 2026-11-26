package kurou.kodriver.feature.otherserveripdetail

import kurou.kodriver.domain.runCapturingErrorsOrNull
import kurou.kodriver.domain.serverconnection.usecase.CheckServerConnectivityUseCase
import kurou.kodriver.domain.serverconnection.usecase.SaveServerIpUseCase

internal sealed interface SaveServerIpResult {
    data object Saved : SaveServerIpResult

    data object InvalidIp : SaveServerIpResult

    data object Unreachable : SaveServerIpResult

    data object SaveFailed : SaveServerIpResult
}

internal class SaveServerIpWithConnectivityCheckUseCase(
    private val validateServerIpAddress: ValidateServerIpAddressUseCase,
    private val checkServerConnectivity: CheckServerConnectivityUseCase,
    private val saveServerIp: SaveServerIpUseCase,
) {
    suspend operator fun invoke(
        ip: String,
        checkConnectivity: Boolean = true,
    ): SaveServerIpResult {
        if (!validateServerIpAddress(ip)) return SaveServerIpResult.InvalidIp
        if (checkConnectivity && !checkServerConnectivity(ip)) return SaveServerIpResult.Unreachable

        return runCapturingErrorsOrNull {
            saveServerIp(ip)
            SaveServerIpResult.Saved
        } ?: SaveServerIpResult.SaveFailed
    }
}
