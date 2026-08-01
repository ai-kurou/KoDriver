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
 * 広告名にはホスト名を使う。ホスト名が FQDN として返る環境では、
 * 先頭ラベルだけを使って Android 側の表示名が長くなりすぎないようにする。
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
