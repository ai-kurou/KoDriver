package kurou.kodriver.core.serverdiscoverydata

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kurou.kodriver.domain.serverconnection.model.DiscoveredServer
import kurou.kodriver.domain.serverconnection.repository.WindowsServerDiscoveryRepository
import java.util.concurrent.atomic.AtomicBoolean

private const val SERVICE_TYPE = "_kodriver._tcp."

internal class NsdWindowsServerDiscoveryRepository(
    private val context: Context,
) : WindowsServerDiscoveryRepository {
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
                        // 登録に失敗した callback は NsdManager に登録されていないため、解除の対象から外す
                        // （解除すると IllegalArgumentException になる）。
                        override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) {
                            serviceInfoCallbacks.remove(serviceInfo.serviceName, this)
                        }

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

            // 開始に失敗すると NsdManager がリスナーの登録を解除するため、停止を呼ぶと
            // IllegalArgumentException になる。失敗を記録し、停止を呼ばないようにする。
            val discoveryStartFailed = AtomicBoolean(false)

            val discoveryListener =
                object : NsdManager.DiscoveryListener {
                    override fun onStartDiscoveryFailed(
                        serviceType: String,
                        errorCode: Int,
                    ) {
                        discoveryStartFailed.set(true)
                    }

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
                if (!discoveryStartFailed.get()) {
                    nsdManager.stopServiceDiscovery(discoveryListener)
                }
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
