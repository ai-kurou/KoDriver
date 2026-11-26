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

            // 旧APIの resolveService は同時に1件しか解決できず、解決中の要求は FAILURE_ALREADY_ACTIVE で
            // 失敗する。発見したサービスをキューに積み、前の解決が終わってから次を解決する。
            val pendingResolves = ArrayDeque<NsdServiceInfo>()
            var resolving = false

            @Suppress("DEPRECATION")
            fun resolveNextLegacy() {
                val next =
                    synchronized(pendingResolves) {
                        if (resolving) return
                        pendingResolves.removeFirstOrNull()?.also { resolving = true }
                    } ?: return
                nsdManager.resolveService(
                    next,
                    object : NsdManager.ResolveListener {
                        override fun onResolveFailed(
                            serviceInfo: NsdServiceInfo,
                            errorCode: Int,
                        ) = finishResolve()

                        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                            updateServer(serviceInfo)
                            finishResolve()
                        }

                        private fun finishResolve() {
                            synchronized(pendingResolves) { resolving = false }
                            resolveNextLegacy()
                        }
                    },
                )
            }

            fun resolveLegacy(serviceInfo: NsdServiceInfo) {
                synchronized(pendingResolves) { pendingResolves.addLast(serviceInfo) }
                resolveNextLegacy()
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
                        // 解決待ちのまま消えたサービスは解決しない（解決後に一覧へ戻さないため）。
                        synchronized(pendingResolves) {
                            pendingResolves.removeAll { it.serviceName == serviceInfo.serviceName }
                        }
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
