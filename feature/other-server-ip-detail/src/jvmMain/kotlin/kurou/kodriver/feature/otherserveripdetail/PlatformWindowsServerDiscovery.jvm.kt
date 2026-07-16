package kurou.kodriver.feature.otherserveripdetail

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kurou.kodriver.domain.MdnsConstants
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.IOException
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener

private val SERVICE_TYPE = MdnsConstants.KO_DRIVER_SERVICE_TYPE

internal class JmdnsWindowsServerDiscovery(
    private val jmdnsFactory: () -> JmDNS = { JmDNS.create(InetAddress.getLocalHost()) },
) : WindowsServerDiscovery {

    override fun discover(): Flow<List<DiscoveredServer>> = callbackFlow {
        val instance = try {
            jmdnsFactory()
        } catch (e: IOException) {
            close()
            return@callbackFlow
        }

        val servers = mutableMapOf<String, DiscoveredServer>()
        val listener = object : ServiceListener {
            override fun serviceAdded(event: ServiceEvent) {
                instance.requestServiceInfo(event.type, event.name)
            }

            override fun serviceRemoved(event: ServiceEvent) {
                servers.remove(event.name)
                trySend(servers.values.toList())
            }

            override fun serviceResolved(event: ServiceEvent) {
                val address = event.info.hostAddresses.firstOrNull() ?: return
                servers[event.name] = DiscoveredServer(hostName = event.info.name, ipAddress = address)
                trySend(servers.values.toList())
            }
        }
        instance.addServiceListener(SERVICE_TYPE, listener)

        awaitClose {
            instance.removeServiceListener(SERVICE_TYPE, listener)
            try {
                instance.close()
            } catch (e: IOException) {
                // no-op: 破棄時のクローズ失敗は無視してよい
            }
        }
    }
}

internal actual val platformWindowsServerDiscoveryModule: Module = module {
    factory<WindowsServerDiscovery> { JmdnsWindowsServerDiscovery() }
}
