package kurou.kodriver.feature.otherserveripdetail

internal class ValidateServerIpAddressUseCase {
    operator fun invoke(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size != 4) return false
        return parts.all { part -> part.toIntOrNull()?.let { it in 0..255 } == true }
    }
}
