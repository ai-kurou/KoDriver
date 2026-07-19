package kurou.kodriver

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo
import kotlin.test.Test

class KoDriverServiceAdvertiserTest {

    @Test
    fun `startするとホスト名でmDNSサービスを登録する`() {
        val jmdns = mockk<JmDNS>(relaxed = true)
        val advertiser = KoDriverServiceAdvertiser(
            jmdnsFactory = { jmdns },
            hostNameProvider = { "my-pc" },
        )

        advertiser.start(port = 8080)

        verify {
            jmdns.registerService(
                withArg<ServiceInfo> {
                    assert(it.type == KoDriverServiceAdvertiser.SERVICE_TYPE)
                    assert(it.name == "my-pc")
                    assert(it.port == 8080)
                },
            )
        }
    }

    @Test
    fun `stopすると登録済みのサービスを解除してクローズする`() {
        val jmdns = mockk<JmDNS>(relaxed = true)
        val advertiser = KoDriverServiceAdvertiser(jmdnsFactory = { jmdns }, hostNameProvider = { "my-pc" })
        advertiser.start(port = 8080)

        advertiser.stop()

        verify {
            jmdns.unregisterAllServices()
            jmdns.close()
        }
    }

    @Test
    fun `FQDNのホスト名はドット以降を除去してサービス名に使う`() {
        val jmdns = mockk<JmDNS>(relaxed = true)
        val advertiser = KoDriverServiceAdvertiser(
            jmdnsFactory = { jmdns },
            hostNameProvider = { "my-pc.local" },
        )

        advertiser.start(port = 8080)

        verify {
            jmdns.registerService(
                withArg<ServiceInfo> {
                    assert(it.name == "my-pc")
                },
            )
        }
    }

    @Test
    fun `startを2回呼ぶと前のインスタンスを解除してから新規登録する`() {
        val firstJmdns = mockk<JmDNS>(relaxed = true)
        val secondJmdns = mockk<JmDNS>(relaxed = true)
        var callCount = 0
        val advertiser = KoDriverServiceAdvertiser(
            jmdnsFactory = { if (callCount++ == 0) firstJmdns else secondJmdns },
            hostNameProvider = { "my-pc" },
        )

        advertiser.start(port = 8080)
        advertiser.start(port = 8081)

        verify {
            firstJmdns.unregisterAllServices()
            firstJmdns.close()
            secondJmdns.registerService(
                withArg<ServiceInfo> {
                    assert(it.type == KoDriverServiceAdvertiser.SERVICE_TYPE)
                    assert(it.name == "my-pc")
                    assert(it.port == 8081)
                },
            )
        }
    }

    @Test
    fun `start前にstopしても何も起きない`() {
        val advertiser = KoDriverServiceAdvertiser(jmdnsFactory = { mockk(relaxed = true) })

        advertiser.stop()
    }

    @Test
    fun `startでIOExceptionが発生しても例外を伝播しない`() {
        val advertiser = KoDriverServiceAdvertiser(
            jmdnsFactory = { throw IOException("network unavailable") },
            hostNameProvider = { "my-pc" },
        )

        advertiser.start(port = 8080)
    }

    @Test
    fun `stopでIOExceptionが発生しても例外を伝播しない`() {
        val jmdns = mockk<JmDNS>(relaxed = true)
        every { jmdns.unregisterAllServices() } throws IOException("close failed")
        val advertiser = KoDriverServiceAdvertiser(jmdnsFactory = { jmdns }, hostNameProvider = { "my-pc" })
        advertiser.start(port = 8080)

        advertiser.stop()
    }
}
