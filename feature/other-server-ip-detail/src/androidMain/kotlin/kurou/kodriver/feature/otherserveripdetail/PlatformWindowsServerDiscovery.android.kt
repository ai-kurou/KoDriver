package kurou.kodriver.feature.otherserveripdetail

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.module.Module
import org.koin.dsl.module

private const val SERVICE_TYPE = "_kodriver._tcp."

internal class NsdWindowsServerDiscovery(
    private val context: Context,
) : WindowsServerDiscovery {
    override fun discover(): Flow<List<DiscoveredServer>> =
        callbackFlow {
            val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
            val servers = mutableMapOf<String, DiscoveredServer>()

            val serviceInfoCallbacks = mutableMapOf<String, NsdManager.ServiceInfoCallback>()

            fun updateServer(serviceInfo: NsdServiceInfo) {
                val address = resolveHostAddress(serviceInfo) ?: return
                servers[serviceInfo.serviceName] =
                    DiscoveredServer(
                        hostName = serviceInfo.serviceName,
                        ipAddress = address,
                    )
                trySend(servers.values.toList())
            }

            @Suppress("DEPRECATION")
            fun resolveLegacy(serviceInfo: NsdServiceInfo) {
                nsdManager.resolveService(
                    serviceInfo,
                    object : NsdManager.ResolveListener {
                        override fun onResolveFailed(
                            serviceInfo: NsdServiceInfo,
                            errorCode: Int,
                        ) = Unit

                        override fun onServiceResolved(serviceInfo: NsdServiceInfo) = updateServer(serviceInfo)
                    },
                )
            }

            fun watchService(serviceInfo: NsdServiceInfo) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    resolveLegacy(serviceInfo)
                    return
                }
                val callback =
                    object : NsdManager.ServiceInfoCallback {
                        override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) = Unit

                        override fun onServiceUpdated(serviceInfo: NsdServiceInfo) = updateServer(serviceInfo)

                        override fun onServiceLost() = Unit

                        override fun onServiceInfoCallbackUnregistered() = Unit
                    }
                serviceInfoCallbacks
                    .put(
                        serviceInfo.serviceName,
                        callback,
                    )?.let(nsdManager::unregisterServiceInfoCallback)
                nsdManager.registerServiceInfoCallback(serviceInfo, { it.run() }, callback)
            }

            val discoveryListener =
                object : NsdManager.DiscoveryListener {
                    override fun onStartDiscoveryFailed(
                        serviceType: String,
                        errorCode: Int,
                    ) = Unit

                    override fun onStopDiscoveryFailed(
                        serviceType: String,
                        errorCode: Int,
                    ) = Unit

                    override fun onDiscoveryStarted(serviceType: String) = Unit

                    override fun onDiscoveryStopped(serviceType: String) = Unit

                    override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                        watchService(serviceInfo)
                    }

                    override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                        serviceInfoCallbacks
                            .remove(
                                serviceInfo.serviceName,
                            )?.let(nsdManager::unregisterServiceInfoCallback)
                        servers.remove(serviceInfo.serviceName)
                        trySend(servers.values.toList())
                    }
                }

            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

            awaitClose {
                serviceInfoCallbacks.values.forEach(nsdManager::unregisterServiceInfoCallback)
                serviceInfoCallbacks.clear()
                nsdManager.stopServiceDiscovery(discoveryListener)
            }
        }
}

@Suppress("DEPRECATION")
private fun resolveHostAddress(serviceInfo: NsdServiceInfo): String? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        serviceInfo.hostAddresses.firstOrNull()?.hostAddress
    } else {
        serviceInfo.host?.hostAddress
    }

internal actual val platformWindowsServerDiscoveryModule: Module =
    module {
        factory<WindowsServerDiscovery> { NsdWindowsServerDiscovery(get<Context>()) }
    }
