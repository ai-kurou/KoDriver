package kurou.kodriver

import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

class KoDriverServiceAdvertiser(
    private val jmdnsFactory: () -> JmDNS = { JmDNS.create(InetAddress.getLocalHost()) },
    private val hostNameProvider: () -> String = { InetAddress.getLocalHost().hostName },
) {
    private var jmdns: JmDNS? = null

    fun start(port: Int) {
        try {
            val instance = jmdnsFactory()
            instance.registerService(ServiceInfo.create(SERVICE_TYPE, hostNameProvider(), port, ""))
            jmdns = instance
        } catch (e: IOException) {
            logger.warn("mDNSサービスの登録に失敗しました", e)
            jmdns = null
        }
    }

    fun stop() {
        try {
            jmdns?.let {
                it.unregisterAllServices()
                it.close()
            }
        } catch (e: IOException) {
            logger.warn("mDNSサービスの停止に失敗しました", e)
        } finally {
            jmdns = null
        }
    }

    companion object {
        const val SERVICE_TYPE = "_kodriver._tcp.local."
        private val logger = LoggerFactory.getLogger(KoDriverServiceAdvertiser::class.java)
    }
}
