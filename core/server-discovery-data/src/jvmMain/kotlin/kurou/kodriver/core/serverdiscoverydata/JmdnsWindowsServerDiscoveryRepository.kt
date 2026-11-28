package kurou.kodriver.core.serverdiscoverydata

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kurou.kodriver.domain.MdnsConstants
import kurou.kodriver.domain.captureError
import kurou.kodriver.domain.serverconnection.model.DiscoveredServer
import kurou.kodriver.domain.serverconnection.repository.WindowsServerDiscoveryRepository
import java.io.IOException
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener

private val SERVICE_TYPE = MdnsConstants.KO_DRIVER_SERVICE_TYPE

internal class JmdnsWindowsServerDiscoveryRepository(
    private val addressesProvider: () -> List<InetAddress> = { mdnsAddresses() },
    private val jmdnsFactory: (InetAddress) -> JmDNS = { JmDNS.create(it) },
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : WindowsServerDiscoveryRepository {
    override fun discover(): Flow<List<DiscoveredServer>> =
        callbackFlow {
            val instances = createInstances()
            if (instances.isEmpty()) {
                close()
                return@callbackFlow
            }

            // 解決の通知は JmDNS インスタンスごとの別スレッドから届く
            val lock = Any()
            val servers = mutableMapOf<Pair<Int, String>, DiscoveredServer>()

            // 同じサーバーが複数のインターフェースで見つかった場合は、先に解決したアドレスだけを出す
            fun sendServers() {
                trySend(servers.values.distinctBy { it.hostName })
            }

            val registrations =
                instances.mapIndexed { index, instance ->
                    val listener =
                        object : ServiceListener {
                            override fun serviceAdded(event: ServiceEvent) {
                                instance.requestServiceInfo(event.type, event.name)
                            }

                            override fun serviceRemoved(event: ServiceEvent) {
                                synchronized(lock) {
                                    servers.remove(index to event.name)
                                    sendServers()
                                }
                            }

                            override fun serviceResolved(event: ServiceEvent) {
                                val address = event.info.hostAddresses.firstOrNull() ?: return
                                synchronized(lock) {
                                    servers[index to event.name] =
                                        DiscoveredServer(hostName = event.info.name, ipAddress = address)
                                    sendServers()
                                }
                            }
                        }
                    instance.addServiceListener(SERVICE_TYPE, listener)
                    instance to listener
                }

            awaitClose {
                registrations.forEach { (instance, listener) ->
                    instance.removeServiceListener(SERVICE_TYPE, listener)
                    try {
                        instance.close()
                    } catch (e: IOException) {
                        // no-op: 破棄時のクローズ失敗は無視してよい
                    }
                }
            }
        }.flowOn(dispatcher)

    /** 対象アドレスごとに JmDNS を生成する。生成に失敗したアドレスは除く。 */
    private fun createInstances(): List<JmDNS> {
        val addresses =
            try {
                addressesProvider()
            } catch (e: IOException) {
                captureError(e)
                emptyList()
            }
        return addresses.mapNotNull { address ->
            try {
                jmdnsFactory(address)
            } catch (e: IOException) {
                captureError(e)
                null
            }
        }
    }
}
