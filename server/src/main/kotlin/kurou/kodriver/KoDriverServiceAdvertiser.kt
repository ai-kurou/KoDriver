package kurou.kodriver

import kurou.kodriver.domain.MdnsConstants
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

/**
 * KoDriver サーバーを mDNS / DNS-SD で LAN 内へ広告する。
 *
 * 広告名にはホスト名を使い、複数台の Windows PC が同一 LAN 上で起動していても
 * Android 側がホスト名で区別できるようにする。ホスト名が FQDN（ドット区切り）として
 * 返る環境向けに、ドット以降を除去してから使用する。
 */
class KoDriverServiceAdvertiser(
    private val jmdnsFactory: () -> JmDNS = { JmDNS.create(InetAddress.getLocalHost()) },
    private val hostNameProvider: () -> String = { InetAddress.getLocalHost().hostName },
) {
    private var jmdns: JmDNS? = null

    /** 指定ポートで KoDriver サービスの広告を開始する。既存広告があれば先に停止する。 */
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

    /** 現在の広告を解除し、JmDNS のソケットを閉じる。 */
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
