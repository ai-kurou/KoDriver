package kurou.kodriver.feature.otherserveripdetail

import kurou.kodriver.domain.model.IPV4_OCTET_MAX
import kurou.kodriver.domain.model.IPV4_OCTET_MIN

internal class ValidateServerIpAddressUseCase {
    operator fun invoke(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size != 4) return false
        return parts.all { part -> part.toIntOrNull()?.let { it in IPV4_OCTET_MIN..IPV4_OCTET_MAX } == true }
    }
}
