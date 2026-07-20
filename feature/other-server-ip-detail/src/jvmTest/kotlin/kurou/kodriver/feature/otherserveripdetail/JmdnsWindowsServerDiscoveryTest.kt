@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherserveripdetail

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.io.IOException
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceInfo
import javax.jmdns.ServiceListener
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class JmdnsWindowsServerDiscoveryTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @RelaxedMockK
    private lateinit var jmdns: JmDNS

    @MockK
    private lateinit var info: ServiceInfo

    @MockK
    private lateinit var event: ServiceEvent

    @MockK
    private lateinit var resolvedEvent: ServiceEvent

    @MockK
    private lateinit var removedEvent: ServiceEvent

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `サービスが解決されるとホスト名とIPアドレスを含むリストを送信する`() = runTest(testDispatcher) {
        var listener: ServiceListener? = null
        every { jmdns.addServiceListener(any(), any()) } answers { listener = secondArg() }
        val discovery = JmdnsWindowsServerDiscovery(jmdnsFactory = { jmdns })

        val results = mutableListOf<List<DiscoveredServer>>()
        val job = launch { discovery.discover().collect { results += it } }

        every { info.name } returns "my-pc"
        every { info.hostAddresses } returns arrayOf("192.168.1.10")
        every { event.getInfo() } returns info
        every { event.getName() } returns "my-pc"
        requireNotNull(listener).serviceResolved(event)
        job.cancelAndJoin()

        assertEquals(listOf(DiscoveredServer("my-pc", "192.168.1.10")), results.last())
    }

    @Test
    fun `サービスが削除されると一覧から除かれる`() = runTest(testDispatcher) {
        var listener: ServiceListener? = null
        every { jmdns.addServiceListener(any(), any()) } answers { listener = secondArg() }
        val discovery = JmdnsWindowsServerDiscovery(jmdnsFactory = { jmdns })

        val results = mutableListOf<List<DiscoveredServer>>()
        val job = launch { discovery.discover().collect { results += it } }

        every { info.name } returns "my-pc"
        every { info.hostAddresses } returns arrayOf("192.168.1.10")
        every { resolvedEvent.getInfo() } returns info
        every { resolvedEvent.getName() } returns "my-pc"
        requireNotNull(listener).serviceResolved(resolvedEvent)

        every { removedEvent.getName() } returns "my-pc"
        requireNotNull(listener).serviceRemoved(removedEvent)
        job.cancelAndJoin()

        assertEquals(emptyList(), results.last())
    }

    @Test
    fun `serviceAddedでrequestServiceInfoが呼ばれる`() = runTest(testDispatcher) {
        var listener: ServiceListener? = null
        every { jmdns.addServiceListener(any(), any()) } answers { listener = secondArg() }
        val discovery = JmdnsWindowsServerDiscovery(jmdnsFactory = { jmdns })

        val job = launch { discovery.discover().collect { } }

        every { event.getType() } returns "_kodriver._tcp.local."
        every { event.getName() } returns "my-pc"
        requireNotNull(listener).serviceAdded(event)
        job.cancelAndJoin()

        verify(exactly = 1) { jmdns.requestServiceInfo("_kodriver._tcp.local.", "my-pc") }
        verify(exactly = 1) { jmdns.addServiceListener(any(), any()) }
        verify(exactly = 1) { jmdns.removeServiceListener(any(), any()) }
        verify(exactly = 1) { jmdns.close() }
        confirmVerified(jmdns)
    }

    @Test
    fun `キャンセルされるとリスナーの解除とJmDNSのクローズが行われる`() = runTest(testDispatcher) {
        val discovery = JmdnsWindowsServerDiscovery(jmdnsFactory = { jmdns })

        val job = launch { discovery.discover().collect { } }
        job.cancelAndJoin()

        verify(exactly = 1) { jmdns.removeServiceListener(any(), any()) }
        verify(exactly = 1) { jmdns.close() }
        verify(exactly = 1) { jmdns.addServiceListener(any(), any()) }
        confirmVerified(jmdns)
    }

    @Test
    fun `JmDNSの生成に失敗しても例外を伝播せず空のフローになる`() = runTest(testDispatcher) {
        val discovery = JmdnsWindowsServerDiscovery(jmdnsFactory = { throw IOException("network unavailable") })

        val results = mutableListOf<List<DiscoveredServer>>()
        discovery.discover().collect { results += it }

        assertTrue(results.isEmpty())
    }

    @Test
    fun `キャンセル時にJmDNSのクローズが失敗しても例外を伝播しない`() = runTest(testDispatcher) {
        every { jmdns.close() } throws IOException("close failed")
        val discovery = JmdnsWindowsServerDiscovery(jmdnsFactory = { jmdns })

        val job = launch { discovery.discover().collect { } }
        job.cancelAndJoin()
    }
}
