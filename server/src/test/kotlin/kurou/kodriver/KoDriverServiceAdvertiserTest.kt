package kurou.kodriver

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.sentry.Sentry
import io.sentry.protocol.SentryId
import java.io.IOException
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo
import kotlin.test.Test

class KoDriverServiceAdvertiserTest {
    private val jmdns: JmDNS = mockk(relaxed = true)

    private val secondJmdns: JmDNS = mockk(relaxed = true)

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
        val exception = IOException("network unavailable")
        mockkStatic(Sentry::class)
        every { Sentry.captureException(exception) } returns SentryId.EMPTY_ID
        try {
            val advertiser =
                KoDriverServiceAdvertiser(
                    jmdnsFactory = { throw exception },
                    suffixProvider = { "AB12" },
                )

            advertiser.start(port = 8080)
            advertiser.stop()

            verify(exactly = 1) { Sentry.captureException(exception) }
            confirmVerified(Sentry::class)
        } finally {
            unmockkStatic(Sentry::class)
        }
    }

    @Test
    fun `サービス登録に失敗した例外を一度だけ報告する`() {
        val exception = IOException("registration failed")
        every {
            jmdns.registerService(
                match {
                    it.type == KoDriverServiceAdvertiser.SERVICE_TYPE &&
                        it.name == "KoDriver-AB12" && it.port == 8080
                },
            )
        } throws exception
        mockkStatic(Sentry::class)
        every { Sentry.captureException(exception) } returns SentryId.EMPTY_ID
        try {
            val advertiser = KoDriverServiceAdvertiser(jmdnsFactory = { jmdns }, suffixProvider = { "AB12" })

            advertiser.start(port = 8080)
            advertiser.stop()

            verify(exactly = 1) {
                jmdns.registerService(
                    withArg<ServiceInfo> {
                        assert(it.type == KoDriverServiceAdvertiser.SERVICE_TYPE)
                        assert(it.name == "KoDriver-AB12")
                        assert(it.port == 8080)
                    },
                )
                Sentry.captureException(exception)
            }
            verify(exactly = 0) { jmdns.unregisterAllServices() }
            verify(exactly = 0) { jmdns.close() }
            confirmVerified(jmdns, Sentry::class)
        } finally {
            unmockkStatic(Sentry::class)
        }
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
