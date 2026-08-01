package kurou.kodriver

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import java.io.IOException
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo
import kotlin.test.BeforeTest
import kotlin.test.Test

class KoDriverServiceAdvertiserTest {
    @RelaxedMockK
    private lateinit var jmdns: JmDNS

    @RelaxedMockK
    private lateinit var secondJmdns: JmDNS

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `startするとホスト名でmDNSサービスを登録する`() {
        val advertiser =
            KoDriverServiceAdvertiser(
                jmdnsFactory = { jmdns },
                hostNameProvider = { "my-pc" },
            )

        advertiser.start(port = 8080)

        verify(exactly = 1) {
            jmdns.registerService(
                withArg<ServiceInfo> {
                    assert(it.type == KoDriverServiceAdvertiser.SERVICE_TYPE)
                    assert(it.name == "my-pc")
                    assert(it.port == 8080)
                },
            )
        }
        confirmVerified(jmdns)
    }

    @Test
    fun `stopすると登録済みのサービスを解除してクローズする`() {
        val advertiser = KoDriverServiceAdvertiser(jmdnsFactory = { jmdns }, hostNameProvider = { "my-pc" })
        advertiser.start(port = 8080)

        advertiser.stop()

        verify(exactly = 1) {
            jmdns.registerService(withArg<ServiceInfo> { assert(it.port == 8080) })
            jmdns.unregisterAllServices()
            jmdns.close()
        }
        confirmVerified(jmdns)
    }

    @Test
    fun `FQDNのホスト名はドット以降を除去してサービス名に使う`() {
        val advertiser =
            KoDriverServiceAdvertiser(
                jmdnsFactory = { jmdns },
                hostNameProvider = { "my-pc.local" },
            )

        advertiser.start(port = 8080)

        verify(exactly = 1) {
            jmdns.registerService(
                withArg<ServiceInfo> {
                    assert(it.name == "my-pc")
                },
            )
        }
        confirmVerified(jmdns)
    }

    @Test
    fun `startを2回呼ぶと前のインスタンスを解除してから新規登録する`() {
        var callCount = 0
        val advertiser =
            KoDriverServiceAdvertiser(
                jmdnsFactory = { if (callCount++ == 0) jmdns else secondJmdns },
                hostNameProvider = { "my-pc" },
            )

        advertiser.start(port = 8080)
        advertiser.start(port = 8081)

        verify(exactly = 1) {
            jmdns.registerService(withArg<ServiceInfo> { assert(it.port == 8080) })
            jmdns.unregisterAllServices()
            jmdns.close()
            secondJmdns.registerService(
                withArg<ServiceInfo> {
                    assert(it.type == KoDriverServiceAdvertiser.SERVICE_TYPE)
                    assert(it.name == "my-pc")
                    assert(it.port == 8081)
                },
            )
        }
        confirmVerified(jmdns, secondJmdns)
    }

    @Test
    fun `start前にstopしても何も起きない`() {
        val advertiser = KoDriverServiceAdvertiser(jmdnsFactory = { jmdns })

        advertiser.stop()

        confirmVerified(jmdns)
    }

    @Test
    fun `startでIOExceptionが発生しても例外を伝播しない`() {
        val advertiser =
            KoDriverServiceAdvertiser(
                jmdnsFactory = { throw IOException("network unavailable") },
                hostNameProvider = { "my-pc" },
            )

        advertiser.start(port = 8080)
    }

    @Test
    fun `stopでIOExceptionが発生しても例外を伝播しない`() {
        every { jmdns.unregisterAllServices() } throws IOException("close failed")
        val advertiser = KoDriverServiceAdvertiser(jmdnsFactory = { jmdns }, hostNameProvider = { "my-pc" })
        advertiser.start(port = 8080)

        advertiser.stop()

        verify(exactly = 1) {
            jmdns.registerService(withArg<ServiceInfo> { assert(it.port == 8080) })
        }
        verify(exactly = 1) { jmdns.unregisterAllServices() }
        confirmVerified(jmdns)
    }
}
