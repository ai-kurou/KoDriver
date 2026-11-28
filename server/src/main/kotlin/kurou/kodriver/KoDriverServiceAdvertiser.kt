package kurou.kodriver

import kurou.kodriver.domain.MdnsConstants
import kurou.kodriver.domain.captureError
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.InetAddress
import java.util.concurrent.Callable
import java.util.concurrent.Executors
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
internal class KoDriverServiceAdvertiser(
    private val addressesProvider: () -> List<InetAddress> = { mdnsAddresses() },
    private val jmdnsFactory: (InetAddress) -> JmDNS = { JmDNS.create(it) },
    suffixProvider: () -> String = { randomSuffix() },
) {
    private var instances: List<JmDNS> = emptyList()
    private val instanceName = "$INSTANCE_NAME_PREFIX${suffixProvider()}"

    /**
     * 指定ポートで KoDriver サービスの広告を開始する。既存広告があれば先に停止する。
     *
     * 対象の各アドレスで広告し、一部のアドレスで失敗しても残りは広告を続ける。
     */
    fun start(port: Int) {
        stop()
        val addresses =
            try {
                addressesProvider()
            } catch (e: IOException) {
                reportRegistrationFailure(e)
                return
            }
        instances = addresses.mapConcurrently { advertise(it, port) }.filterNotNull()
    }

    /** 現在の広告を解除し、JmDNS のソケットを閉じる。 */
    fun stop() {
        instances.mapConcurrently { withdraw(it) }
        instances = emptyList()
    }

    private fun advertise(
        address: InetAddress,
        port: Int,
    ): JmDNS? {
        var instance: JmDNS? = null
        return try {
            instance = jmdnsFactory(address)
            // ServiceInfo は登録先の JmDNS が状態を持たせるため、インスタンスごとに生成する
            instance.registerService(ServiceInfo.create(SERVICE_TYPE, instanceName, port, ""))
            instance
        } catch (e: IOException) {
            reportRegistrationFailure(e)
            instance?.let { withdraw(it) }
            null
        }
    }

    private fun withdraw(instance: JmDNS) {
        try {
            instance.use { it.unregisterAllServices() }
        } catch (e: IOException) {
            logger.warn("mDNSサービスの停止に失敗しました", e)
            captureError(e)
        }
    }

    private fun reportRegistrationFailure(e: IOException) {
        logger.warn("mDNSサービスの登録に失敗しました", e)
        captureError(e)
    }

    /** 登録と解除は完了まで数秒ブロックするため、アドレスの数だけ起動・終了が遅くならないよう並行して実行する。 */
    private fun <T, R> List<T>.mapConcurrently(transform: (T) -> R): List<R> {
        if (isEmpty()) return emptyList()
        val executor = Executors.newFixedThreadPool(size)
        return try {
            executor.invokeAll(map { item -> Callable { transform(item) } }).map { it.get() }
        } finally {
            executor.shutdown()
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
