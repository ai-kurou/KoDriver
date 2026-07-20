@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.main

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository
import kurou.kodriver.domain.repository.ServerVersionRepository
import kurou.kodriver.domain.usecase.FetchServerVersionUseCase
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuServerBannerConnectionCheckerTest {

    @MockK
    private lateinit var ipRepository: ServerIpPreferencesRepository

    @MockK
    private lateinit var versionRepository: ServerVersionRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `IPアドレスが未設定の場合はIP_NOT_CONFIGUREDを返す`() = runTest {
        val checker = createChecker(ip = null)

        val status = checker.statusFlow().first()

        assertEquals(ConnectionBannerVmStatus.IP_NOT_CONFIGURED, status)
    }

    @Test
    fun `IPアドレスが設定されサーバー取得に成功するとCONNECTEDを返す`() = runTest {
        val checker = createChecker(ip = "192.168.1.1", versionResult = Result.success("1.0.0"))

        val statuses = checker.statusFlow().take(2).toList()

        assertEquals(ConnectionBannerVmStatus.UNCHECKED, statuses[0])
        assertEquals(ConnectionBannerVmStatus.CONNECTED, statuses[1])
        coVerify(exactly = 1) { versionRepository.fetchVersion("192.168.1.1") }
    }

    @Test
    fun `IPアドレスが設定されサーバー取得に失敗するとDISCONNECTEDを返す`() = runTest {
        val checker = createChecker(ip = "192.168.1.1", versionResult = Result.failure(Exception("error")))

        val statuses = checker.statusFlow().take(2).toList()

        assertEquals(ConnectionBannerVmStatus.UNCHECKED, statuses[0])
        assertEquals(ConnectionBannerVmStatus.DISCONNECTED, statuses[1])
        coVerify(exactly = 1) { versionRepository.fetchVersion("192.168.1.1") }
    }

    @Test
    fun `IPアドレスがnullからIPが設定されるとIP_NOT_CONFIGUREDからCONNECTEDへ遷移する`() =
        runTest(UnconfinedTestDispatcher()) {
            val ipFlow = MutableStateFlow<String?>(null)
            val checker = createChecker(ipFlow = ipFlow, versionResult = Result.success("1.0.0"))
            val emitted = mutableListOf<ConnectionBannerVmStatus>()
            val job = launch { checker.statusFlow().collect { emitted.add(it) } }

            assertEquals(ConnectionBannerVmStatus.IP_NOT_CONFIGURED, emitted.last())

            ipFlow.value = "192.168.1.1"

            assertEquals(ConnectionBannerVmStatus.CONNECTED, emitted.last())
            coVerify(exactly = 1) { versionRepository.fetchVersion("192.168.1.1") }
            job.cancel()
        }

    private fun createChecker(
        ip: String? = null,
        versionResult: Result<String> = Result.success("1.0.0"),
        ipRepository: ServerIpPreferencesRepository = this.ipRepository,
    ): LmuServerBannerConnectionChecker {
        return createChecker(ipFlow = MutableStateFlow(ip), versionResult = versionResult, ipRepository = ipRepository)
    }

    private fun createChecker(
        ipFlow: MutableStateFlow<String?>,
        versionResult: Result<String>,
        ipRepository: ServerIpPreferencesRepository = this.ipRepository,
    ): LmuServerBannerConnectionChecker {
        every { ipRepository.serverIp() } returns ipFlow
        coEvery { versionRepository.fetchVersion("192.168.1.1") } returns versionResult
        return LmuServerBannerConnectionChecker(
            fetchServerVersion = FetchServerVersionUseCase(versionRepository),
            observeServerIp = ObserveServerIpUseCase(ipRepository),
        )
    }
}
