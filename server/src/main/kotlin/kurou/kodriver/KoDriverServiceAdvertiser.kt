package kurou.kodriver

import kurou.kodriver.domain.MdnsConstants
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
        stop()
        try {
            val instance = jmdnsFactory()
            instance.registerService(ServiceInfo.create(SERVICE_TYPE, sanitizedHostName(), port, ""))
            jmdns = instance
        } catch (e: IOException) {
            logger.warn("mDNSサービスの登録に失敗しました", e)
            jmdns = null
        }
    }

    private fun sanitizedHostName(): String = hostNameProvider().substringBefore(".")

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
        const val SERVICE_TYPE = MdnsConstants.KO_DRIVER_SERVICE_TYPE
        private val logger = LoggerFactory.getLogger(KoDriverServiceAdvertiser::class.java)
    }
}
