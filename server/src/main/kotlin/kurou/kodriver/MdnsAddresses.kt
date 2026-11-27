package kurou.kodriver

import kurou.kodriver.domain.captureError
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException

/**
 * mDNS の告知に使う IPv4 アドレスを返す。
 *
 * `InetAddress.getLocalHost()` は Hyper-V / WSL / VPN などの仮想 NIC のアドレスを返すことがあるため、
 * 起動中でマルチキャストに対応するインターフェースのアドレスをすべて対象にする（ループバックとリンクローカルは除く）。
 * 仮想 NIC と LAN は確実に見分けられないので、1つには絞らない。
 * 対象がない・列挙できない場合は `getLocalHost()` のアドレスを使う。
 */
internal fun mdnsAddresses(
    interfacesProvider: () -> List<NetworkInterface> = { NetworkInterface.getNetworkInterfaces()?.toList().orEmpty() },
    localHostProvider: () -> InetAddress = { InetAddress.getLocalHost() },
): List<InetAddress> {
    val interfaces =
        try {
            interfacesProvider()
        } catch (e: SocketException) {
            captureError(e)
            emptyList()
        }
    return interfaces
        .flatMap { it.usableIpv4Addresses() }
        .distinct()
        .ifEmpty { listOf(localHostProvider()) }
}

private fun NetworkInterface.usableIpv4Addresses(): List<InetAddress> =
    try {
        if (isUp && !isLoopback && supportsMulticast()) {
            inetAddresses.toList().filter { it is Inet4Address && !it.isLinkLocalAddress }
        } else {
            emptyList()
        }
    } catch (e: SocketException) {
        // 状態を取得できないインターフェースだけを除き、残りは使う
        captureError(e)
        emptyList()
    }
