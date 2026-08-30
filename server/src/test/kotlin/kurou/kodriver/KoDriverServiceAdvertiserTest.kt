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
    fun `startするとKoDriverプレフィックス＋サフィックスでmDNSサービスを登録する`() {
        val advertiser =
            KoDriverServiceAdvertiser(
                jmdnsFactory = { jmdns },
                suffixProvider = { "AB12" },
            )

        advertiser.start(port = 8080)

        verify(exactly = 1) {
            jmdns.registerService(
                withArg<ServiceInfo> {
                    assert(it.type == KoDriverServiceAdvertiser.SERVICE_TYPE)
                    assert(it.name == "KoDriver-AB12")
                    assert(it.port == 8080)
                },
            )
        }
        confirmVerified(jmdns)
    }

    @Test
    fun `stopすると登録済みのサービスを解除してクローズする`() {
        val advertiser = KoDriverServiceAdvertiser(jmdnsFactory = { jmdns }, suffixProvider = { "AB12" })
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
    fun `startを2回呼んでも同一のサフィックスを維持する`() {
        var callCount = 0
        val advertiser =
            KoDriverServiceAdvertiser(
                jmdnsFactory = { if (callCount++ == 0) jmdns else secondJmdns },
                suffixProvider = { "AB12" },
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
                    assert(it.name == "KoDriver-AB12")
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
                suffixProvider = { "AB12" },
            )

        advertiser.start(port = 8080)
    }

    @Test
    fun `suffixProvider省略時はKoDriver-英数字4桁形式のランダムな名前で登録する`() {
        val advertiser = KoDriverServiceAdvertiser(jmdnsFactory = { jmdns })

        advertiser.start(port = 8080)

        verify(exactly = 1) {
            jmdns.registerService(
                withArg<ServiceInfo> {
                    assert(Regex("KoDriver-[A-Z0-9]{4}").matches(it.name)) { "unexpected name: ${it.name}" }
                },
            )
        }
        confirmVerified(jmdns)
    }

    @Test
    fun `stopでIOExceptionが発生しても例外を伝播しない`() {
        every { jmdns.unregisterAllServices() } throws IOException("close failed")
        val advertiser = KoDriverServiceAdvertiser(jmdnsFactory = { jmdns }, suffixProvider = { "AB12" })
        advertiser.start(port = 8080)

        advertiser.stop()

        verify(exactly = 1) {
            jmdns.registerService(withArg<ServiceInfo> { assert(it.port == 8080) })
        }
        verify(exactly = 1) { jmdns.unregisterAllServices() }
        confirmVerified(jmdns)
    }
}
