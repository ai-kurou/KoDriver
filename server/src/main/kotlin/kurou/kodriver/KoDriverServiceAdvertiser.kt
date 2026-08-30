package kurou.kodriver

import kurou.kodriver.domain.MdnsConstants
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo
import kotlin.random.Random

/**
 * KoDriver サーバーを mDNS / DNS-SD で LAN 内へ広告する。
 *
 * 広告名には OS のホスト名（PC所有者名などの個人情報を含みうる）を使わず、
 * 固定プレフィックス＋ランダムサフィックスのアプリ固有識別子を使う。これにより、
 * 複数台の Windows PC が同一 LAN 上で起動していても Android 側が広告名で区別できる。
 * ランダムサフィックスはインスタンス生成時に一度だけ生成し、サーバーが再起動するまで
 * 同一の値を維持する。
 */
class KoDriverServiceAdvertiser(
    private val jmdnsFactory: () -> JmDNS = { JmDNS.create(InetAddress.getLocalHost()) },
    suffixProvider: () -> String = { randomSuffix() },
) {
    private var jmdns: JmDNS? = null
    private val instanceName = "$INSTANCE_NAME_PREFIX${suffixProvider()}"

    /** 指定ポートで KoDriver サービスの広告を開始する。既存広告があれば先に停止する。 */
    fun start(port: Int) {
        stop()
        try {
            val instance = jmdnsFactory()
            instance.registerService(ServiceInfo.create(SERVICE_TYPE, instanceName, port, ""))
            jmdns = instance
        } catch (e: IOException) {
            logger.warn("mDNSサービスの登録に失敗しました", e)
            jmdns = null
        }
    }

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
        private const val INSTANCE_NAME_PREFIX = "KoDriver-"
        private const val SUFFIX_LENGTH = 4
        private val SUFFIX_CHARS = ('A'..'Z') + ('0'..'9')
        private val logger = LoggerFactory.getLogger(KoDriverServiceAdvertiser::class.java)

        private fun randomSuffix(): String =
            (1..SUFFIX_LENGTH).joinToString("") { SUFFIX_CHARS.random(Random).toString() }
    }
}
