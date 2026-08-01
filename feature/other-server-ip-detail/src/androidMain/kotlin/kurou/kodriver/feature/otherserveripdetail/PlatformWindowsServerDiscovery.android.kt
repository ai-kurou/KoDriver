package kurou.kodriver.feature.otherserveripdetail

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.module.Module
import org.koin.dsl.module

private const val SERVICE_TYPE = "_kodriver._tcp."

internal class NsdWindowsServerDiscovery(
    private val context: Context,
) : WindowsServerDiscovery {

    override fun discover(): Flow<List<DiscoveredServer>> = callbackFlow {
        val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        val servers = mutableMapOf<String, DiscoveredServer>()

        fun createResolveListener(): NsdManager.ResolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) = Unit

            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                val address = serviceInfo.host?.hostAddress ?: return
                servers[serviceInfo.serviceName] = DiscoveredServer(
                    hostName = serviceInfo.serviceName,
                    ipAddress = address,
                )
                trySend(servers.values.toList())
            }
        }

        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) = Unit

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) = Unit

            override fun onDiscoveryStarted(serviceType: String) = Unit

            override fun onDiscoveryStopped(serviceType: String) = Unit

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                nsdManager.resolveService(serviceInfo, createResolveListener())
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                servers.remove(serviceInfo.serviceName)
                trySend(servers.values.toList())
            }
        }

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

        awaitClose {
            nsdManager.stopServiceDiscovery(discoveryListener)
        }
    }
}

internal actual val platformWindowsServerDiscoveryModule: Module = module {
    factory<WindowsServerDiscovery> { NsdWindowsServerDiscovery(get<Context>()) }
}
