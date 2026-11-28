package kurou.kodriver.core.serverdiscoverydata

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import io.sentry.Sentry
import io.sentry.protocol.SentryId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.serverconnection.model.DiscoveredServer
import java.io.IOException
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceInfo
import javax.jmdns.ServiceListener
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class JmdnsWindowsServerDiscoveryRepositoryTest {
    private companion object {
        const val SERVICE_TYPE = "_kodriver._tcp.local."
    }

    private val testDispatcher = UnconfinedTestDispatcher()

    private val jmdns: JmDNS = mockk(relaxed = true)

    private val info: ServiceInfo = mockk()

    private val event: ServiceEvent = mockk()

    private val resolvedEvent: ServiceEvent = mockk()

    private val removedEvent: ServiceEvent = mockk()

    private val secondJmdns: JmDNS = mockk(relaxed = true)

    private val address: InetAddress = InetAddress.getByName("192.168.1.10")

    private val secondAddress: InetAddress = InetAddress.getByName("172.20.0.1")

    private fun repository(
        addressesProvider: () -> List<InetAddress> = { listOf(address) },
        jmdnsFactory: (InetAddress) -> JmDNS,
    ) = JmdnsWindowsServerDiscoveryRepository(addressesProvider, jmdnsFactory, testDispatcher)

    @Test
    fun `サービスが解決されるとホスト名とIPアドレスを含むリストを送信する`() =
        runTest(testDispatcher) {
            val listenerSlot = slot<ServiceListener>()
            every { jmdns.addServiceListener(SERVICE_TYPE, capture(listenerSlot)) } returns Unit
            val discovery = repository { jmdns }

            val results = mutableListOf<List<DiscoveredServer>>()
            val job = launch { discovery.discover().collect { results += it } }

            every { info.name } returns "my-pc"
            every { info.hostAddresses } returns arrayOf("192.168.1.10")
            every { event.getInfo() } returns info
            every { event.getName() } returns "my-pc"
            listenerSlot.captured.serviceResolved(event)
            job.cancelAndJoin()

            assertEquals(listOf(DiscoveredServer("my-pc", "192.168.1.10")), results.last())
        }

    @Test
    fun `サービスが削除されると一覧から除かれる`() =
        runTest(testDispatcher) {
            val listenerSlot = slot<ServiceListener>()
            every { jmdns.addServiceListener(SERVICE_TYPE, capture(listenerSlot)) } returns Unit
            val discovery = repository { jmdns }

            val results = mutableListOf<List<DiscoveredServer>>()
            val job = launch { discovery.discover().collect { results += it } }

            every { info.name } returns "my-pc"
            every { info.hostAddresses } returns arrayOf("192.168.1.10")
            every { resolvedEvent.getInfo() } returns info
            every { resolvedEvent.getName() } returns "my-pc"
            listenerSlot.captured.serviceResolved(resolvedEvent)

            every { removedEvent.getName() } returns "my-pc"
            listenerSlot.captured.serviceRemoved(removedEvent)
            job.cancelAndJoin()

            assertEquals(emptyList(), results.last())
        }

    @Test
    fun `serviceAddedでrequestServiceInfoが呼ばれる`() =
        runTest(testDispatcher) {
            val listenerSlot = slot<ServiceListener>()
            every { jmdns.addServiceListener(SERVICE_TYPE, capture(listenerSlot)) } returns Unit
            val discovery = repository { jmdns }

            val job = launch { discovery.discover().collect { } }

            every { event.getType() } returns SERVICE_TYPE
            every { event.getName() } returns "my-pc"
            listenerSlot.captured.serviceAdded(event)
            job.cancelAndJoin()

            verify(exactly = 1) { jmdns.requestServiceInfo(SERVICE_TYPE, "my-pc") }
            verify(exactly = 1) { jmdns.addServiceListener(SERVICE_TYPE, listenerSlot.captured) }
            verify(exactly = 1) { jmdns.removeServiceListener(SERVICE_TYPE, listenerSlot.captured) }
            verify(exactly = 1) { jmdns.close() }
            confirmVerified(jmdns)
        }

    @Test
    fun `キャンセルされるとリスナーの解除とJmDNSのクローズが行われる`() =
        runTest(testDispatcher) {
            val listenerSlot = slot<ServiceListener>()
            every { jmdns.addServiceListener(SERVICE_TYPE, capture(listenerSlot)) } returns Unit
            val discovery = repository { jmdns }

            val job = launch { discovery.discover().collect { } }
            job.cancelAndJoin()

            verify(exactly = 1) { jmdns.removeServiceListener(SERVICE_TYPE, listenerSlot.captured) }
            verify(exactly = 1) { jmdns.close() }
            verify(exactly = 1) { jmdns.addServiceListener(SERVICE_TYPE, listenerSlot.captured) }
            confirmVerified(jmdns)
        }

    @Test
    fun `JmDNSの生成に失敗しても例外を伝播せず空のフローになる`() =
        runTest(testDispatcher) {
            val exception = IOException("network unavailable")
            mockkStatic(Sentry::class)
            every { Sentry.captureException(exception) } returns SentryId.EMPTY_ID
            try {
                val discovery = repository { throw exception }
                val results = mutableListOf<List<DiscoveredServer>>()
                discovery.discover().collect { results += it }

                assertTrue(results.isEmpty())
                verify(exactly = 1) { Sentry.captureException(exception) }
                confirmVerified(Sentry::class)
            } finally {
                unmockkStatic(Sentry::class)
            }
        }

    @Test
    fun `キャンセル時にJmDNSのクローズが失敗しても例外を伝播しない`() =
        runTest(testDispatcher) {
            every { jmdns.close() } throws IOException("close failed")
            val discovery = repository { jmdns }

            val job = launch { discovery.discover().collect { } }
            job.cancelAndJoin()
        }

    @Test
    fun `複数のアドレスで同じサーバーが見つかると1件にまとめ片方が消えても残りを出す`() =
        runTest(testDispatcher) {
            val listenerSlot = slot<ServiceListener>()
            val secondListenerSlot = slot<ServiceListener>()
            every { jmdns.addServiceListener(SERVICE_TYPE, capture(listenerSlot)) } returns Unit
            every { secondJmdns.addServiceListener(SERVICE_TYPE, capture(secondListenerSlot)) } returns Unit
            val discovery =
                repository(addressesProvider = { listOf(address, secondAddress) }) {
                    if (it == address) jmdns else secondJmdns
                }
            val results = mutableListOf<List<DiscoveredServer>>()
            val job = launch { discovery.discover().collect { results += it } }

            every { info.name } returns "my-pc"
            every { info.hostAddresses } returnsMany listOf(arrayOf("192.168.1.10"), arrayOf("172.20.0.1"))
            every { event.getInfo() } returns info
            every { event.getName() } returns "my-pc"
            listenerSlot.captured.serviceResolved(event)
            secondListenerSlot.captured.serviceResolved(event)

            assertEquals(listOf(DiscoveredServer("my-pc", "192.168.1.10")), results.last())

            listenerSlot.captured.serviceRemoved(event)

            assertEquals(listOf(DiscoveredServer("my-pc", "172.20.0.1")), results.last())
            job.cancelAndJoin()
            listOf(jmdns to listenerSlot, secondJmdns to secondListenerSlot).forEach { (instance, slot) ->
                verify(exactly = 1) {
                    instance.addServiceListener(SERVICE_TYPE, slot.captured)
                    instance.removeServiceListener(SERVICE_TYPE, slot.captured)
                    instance.close()
                }
            }
            confirmVerified(jmdns, secondJmdns)
        }

    @Test
    fun `アドレスの取得に失敗すると空のフローになる`() =
        runTest(testDispatcher) {
            val exception = IOException("no address")
            mockkStatic(Sentry::class)
            every { Sentry.captureException(exception) } returns SentryId.EMPTY_ID
            try {
                val discovery = repository(addressesProvider = { throw exception }) { jmdns }
                val results = mutableListOf<List<DiscoveredServer>>()
                discovery.discover().collect { results += it }

                assertTrue(results.isEmpty())
                verify(exactly = 1) { Sentry.captureException(exception) }
                confirmVerified(jmdns, Sentry::class)
            } finally {
                unmockkStatic(Sentry::class)
            }
        }
}
