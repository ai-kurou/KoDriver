@file:Suppress("FunctionNaming", "DEPRECATION")

package kurou.kodriver.feature.otherserveripdetail

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.InetAddress
import java.util.concurrent.Executor
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class NsdWindowsServerDiscoveryTest {
    private val nsdManager: NsdManager = mockk()
    private val context: Context = mockk()
    private val discoveryListener = slot<NsdManager.DiscoveryListener>()

    private fun setUpManager() {
        every { context.getSystemService(Context.NSD_SERVICE) } returns nsdManager
        every {
            nsdManager.discoverServices("_kodriver._tcp.", NsdManager.PROTOCOL_DNS_SD, capture(discoveryListener))
        } just runs
        every { nsdManager.stopServiceDiscovery(any<NsdManager.DiscoveryListener>()) } just runs
    }

    private fun serviceInfo(
        name: String,
        host: InetAddress? = null,
    ): NsdServiceInfo =
        mockk {
            every { serviceName } returns name
            every { this@mockk.host } returns host
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                every { hostAddresses } returns listOfNotNull(host)
            }
        }

    private fun address(ip: String): InetAddress =
        InetAddress.getByAddress(
            ip
                .split(".")
                .map {
                    it.toInt().toByte()
                }.toByteArray(),
        )

    private fun runDiscovery(block: suspend (List<List<DiscoveredServer>>) -> Unit) =
        runBlocking {
            setUpManager()
            val emissions = mutableListOf<List<DiscoveredServer>>()
            val job =
                launch(Dispatchers.Unconfined, start = CoroutineStart.UNDISPATCHED) {
                    NsdWindowsServerDiscovery(context).discover().collect { emissions += it }
                }
            block(emissions)
            job.cancel()
            job.join()
        }

    @Test
    @Config(sdk = [28])
    fun `旧APIではサービス発見後にresolveしたホストを通知し失敗時は通知しない`() =
        runDiscovery { emissions ->
            val resolveListener = slot<NsdManager.ResolveListener>()
            every { nsdManager.resolveService(any<NsdServiceInfo>(), capture(resolveListener)) } just runs
            val found = serviceInfo("server")
            val resolved = serviceInfo("server", address("192.168.0.5"))

            discoveryListener.captured.onServiceFound(found)
            resolveListener.captured.onResolveFailed(found, 0)
            resolveListener.captured.onServiceResolved(serviceInfo("server"))
            yield()
            assertTrue(emissions.isEmpty())

            resolveListener.captured.onServiceResolved(resolved)
            yield()
            assertEquals(listOf(listOf(DiscoveredServer("server", "192.168.0.5"))), emissions)

            discoveryListener.captured.onStartDiscoveryFailed("t", 0)
            discoveryListener.captured.onStopDiscoveryFailed("t", 0)
            discoveryListener.captured.onDiscoveryStarted("t")
            discoveryListener.captured.onDiscoveryStopped("t")
            discoveryListener.captured.onServiceLost(found)
            yield()
            assertEquals(emptyList(), emissions.last())

            verify(exactly = 1) { nsdManager.resolveService(found, resolveListener.captured) }
        }

    @Test
    @Config(sdk = [36])
    fun `新APIではサービス更新ごとにホストを通知し同名の再発見で古いコールバックを解除する`() =
        runDiscovery { emissions ->
            val callbacks = mutableListOf<NsdManager.ServiceInfoCallback>()
            every {
                nsdManager.registerServiceInfoCallback(any<NsdServiceInfo>(), any<Executor>(), capture(callbacks))
            } just runs
            every { nsdManager.unregisterServiceInfoCallback(any<NsdManager.ServiceInfoCallback>()) } just runs
            val found = serviceInfo("server")

            discoveryListener.captured.onServiceFound(found)
            discoveryListener.captured.onServiceFound(found)
            val first = callbacks[0]
            val second = callbacks[1]
            verify(exactly = 1) { nsdManager.unregisterServiceInfoCallback(first) }

            second.onServiceUpdated(serviceInfo("server"))
            yield()
            assertTrue(emissions.isEmpty())

            second.onServiceUpdated(serviceInfo("server", address("10.0.0.2")))
            second.onServiceInfoCallbackRegistrationFailed(0)
            second.onServiceLost()
            second.onServiceInfoCallbackUnregistered()
            yield()
            assertEquals(listOf(listOf(DiscoveredServer("server", "10.0.0.2"))), emissions)
        }

    @Test
    @Config(sdk = [36])
    fun `サービス消失でコールバックを解除して一覧から削除し収集終了でディスカバリを停止する`() =
        runBlocking {
            setUpManager()
            val callback = slot<NsdManager.ServiceInfoCallback>()
            val executor = slot<Executor>()
            every {
                nsdManager.registerServiceInfoCallback(any<NsdServiceInfo>(), capture(executor), capture(callback))
            } just runs
            every { nsdManager.unregisterServiceInfoCallback(any<NsdManager.ServiceInfoCallback>()) } just runs
            val emissions = mutableListOf<List<DiscoveredServer>>()
            val job =
                launch(Dispatchers.Unconfined, start = CoroutineStart.UNDISPATCHED) {
                    NsdWindowsServerDiscovery(context).discover().collect { emissions += it }
                }
            val found = serviceInfo("a")
            val other = serviceInfo("b")

            discoveryListener.captured.onServiceFound(found)
            var ran = false
            executor.captured.execute { ran = true }
            assertTrue(ran)
            val callbackA = callback.captured
            callbackA.onServiceUpdated(serviceInfo("a", address("10.0.0.1")))
            discoveryListener.captured.onServiceFound(other)
            val callbackB = callback.captured
            callbackB.onServiceUpdated(serviceInfo("b", address("10.0.0.2")))
            yield()
            assertEquals(2, emissions.last().size)

            discoveryListener.captured.onServiceLost(found)
            yield()
            assertEquals(listOf(DiscoveredServer("b", "10.0.0.2")), emissions.last())
            verify(exactly = 1) { nsdManager.unregisterServiceInfoCallback(callbackA) }

            job.cancel()
            job.join()
            verify(exactly = 1) { nsdManager.unregisterServiceInfoCallback(callbackB) }
            verify(exactly = 1) { nsdManager.stopServiceDiscovery(discoveryListener.captured) }
            verify(exactly = 1) { nsdManager.registerServiceInfoCallback(found, executor.captured, callbackA) }
            verify(exactly = 1) { nsdManager.registerServiceInfoCallback(other, executor.captured, callbackB) }
            verify(exactly = 1) {
                nsdManager.discoverServices("_kodriver._tcp.", NsdManager.PROTOCOL_DNS_SD, discoveryListener.captured)
            }
            confirmVerified(nsdManager)
        }
}
