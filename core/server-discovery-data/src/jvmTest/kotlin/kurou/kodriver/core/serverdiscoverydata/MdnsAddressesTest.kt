package kurou.kodriver.core.serverdiscoverydata

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.sentry.Sentry
import io.sentry.protocol.SentryId
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Collections
import kotlin.test.Test
import kotlin.test.assertEquals

class MdnsAddressesTest {
    private val lan: InetAddress = InetAddress.getByName("192.168.1.10")

    private val virtual: InetAddress = InetAddress.getByName("172.20.0.1")

    private val localHost: InetAddress = InetAddress.getByName("10.0.0.1")

    private fun networkInterface(
        vararg addresses: InetAddress,
        up: Boolean = true,
        loopback: Boolean = false,
        multicast: Boolean = true,
    ): NetworkInterface =
        mockk {
            every { isUp } returns up
            every { isLoopback } returns loopback
            every { supportsMulticast() } returns multicast
            every { inetAddresses } returns Collections.enumeration(addresses.toList())
        }

    @Test
    fun `使えるすべてのインターフェースのアドレスを返す`() {
        val interfaces = listOf(networkInterface(virtual), networkInterface(lan))

        assertEquals(listOf(virtual, lan), mdnsAddresses({ interfaces }, { localHost }))
    }

    @Test
    fun `IPv6とリンクローカルと重複したアドレスは除く`() {
        val interfaces =
            listOf(
                networkInterface(InetAddress.getByName("2001:db8::1"), InetAddress.getByName("169.254.1.1"), lan),
                networkInterface(lan),
            )

        assertEquals(listOf(lan), mdnsAddresses({ interfaces }, { localHost }))
    }

    @Test
    fun `停止中とループバックとマルチキャスト非対応のインターフェースは除く`() {
        val interfaces =
            listOf(
                networkInterface(virtual, up = false),
                networkInterface(virtual, loopback = true),
                networkInterface(virtual, multicast = false),
                networkInterface(lan),
            )

        assertEquals(listOf(lan), mdnsAddresses({ interfaces }, { localHost }))
    }

    @Test
    fun `対象がない場合はローカルホストのアドレスを返す`() {
        assertEquals(listOf(localHost), mdnsAddresses({ emptyList() }, { localHost }))
    }

    @Test
    fun `一部のインターフェースの状態を取得できなくても残りを返す`() {
        val exception = SocketException("state unavailable")
        val broken: NetworkInterface = mockk { every { isUp } throws exception }
        mockkStatic(Sentry::class)
        every { Sentry.captureException(exception) } returns SentryId.EMPTY_ID
        try {
            val interfaces = listOf(broken, networkInterface(lan))

            assertEquals(listOf(lan), mdnsAddresses({ interfaces }, { localHost }))
            verify(exactly = 1) { Sentry.captureException(exception) }
            confirmVerified(Sentry::class)
        } finally {
            unmockkStatic(Sentry::class)
        }
    }

    @Test
    fun `インターフェースを列挙できない場合はローカルホストのアドレスを返す`() {
        val exception = SocketException("enumeration failed")
        mockkStatic(Sentry::class)
        every { Sentry.captureException(exception) } returns SentryId.EMPTY_ID
        try {
            assertEquals(listOf(localHost), mdnsAddresses({ throw exception }, { localHost }))
            verify(exactly = 1) { Sentry.captureException(exception) }
            confirmVerified(Sentry::class)
        } finally {
            unmockkStatic(Sentry::class)
        }
    }
}
