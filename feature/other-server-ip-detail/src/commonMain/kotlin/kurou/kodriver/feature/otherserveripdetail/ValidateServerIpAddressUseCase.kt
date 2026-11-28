package kurou.kodriver.feature.otherserveripdetail

import kurou.kodriver.domain.serverconnection.model.parseIpv4Octets

internal class ValidateServerIpAddressUseCase {
    operator fun invoke(ip: String): Boolean = parseIpv4Octets(ip) != null
}
