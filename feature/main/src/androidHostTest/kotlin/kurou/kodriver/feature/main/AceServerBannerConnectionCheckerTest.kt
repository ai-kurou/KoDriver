@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.main

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository
import kurou.kodriver.domain.repository.ServerVersionRepository
import kurou.kodriver.domain.usecase.FetchServerVersionUseCase
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase
import org.junit.Test
import kotlin.test.assertEquals

class AceServerBannerConnectionCheckerTest : ServerBannerConnectionCheckerTestContract() {
    override fun createStatusFlow(
        fetchServerVersion: FetchServerVersionUseCase,
        observeServerIp: ObserveServerIpUseCase,
    ): Flow<ConnectionBannerVmStatus> =
        AceServerBannerConnectionChecker(fetchServerVersion, observeServerIp).statusFlow()
}

// 両Checkerに同じ4シナリオを適用する共通テスト契約。
@OptIn(ExperimentalCoroutinesApi::class)
abstract class ServerBannerConnectionCheckerTestContract {
    private val ipRepository: ServerIpPreferencesRepository = mockk()

    private val versionRepository: ServerVersionRepository = mockk()

    @Test
    fun `IPアドレスが未設定の場合はIP_NOT_CONFIGUREDを返す`() =
        runTest {
            every { ipRepository.serverIp() } returns MutableStateFlow(null)
            val statusesFlow = createStatusFlow()

            val status = statusesFlow.first()

            assertEquals(ConnectionBannerVmStatus.IP_NOT_CONFIGURED, status)
        }

    @Test
    fun `IPアドレスが設定されサーバー取得に成功するとCONNECTEDを返す`() =
        runTest {
            every { ipRepository.serverIp() } returns MutableStateFlow("192.168.1.1")
            coEvery { versionRepository.fetchVersion("192.168.1.1") } returns Result.success("1.0.0")
            val statusesFlow = createStatusFlow()

            val statuses = statusesFlow.take(2).toList()

            assertEquals(ConnectionBannerVmStatus.UNCHECKED, statuses[0])
            assertEquals(ConnectionBannerVmStatus.CONNECTED, statuses[1])
            coVerify(exactly = 1) { versionRepository.fetchVersion("192.168.1.1") }
            confirmVerified(versionRepository)
        }

    @Test
    fun `IPアドレスが設定されサーバー取得に失敗するとDISCONNECTEDを返す`() =
        runTest {
            every { ipRepository.serverIp() } returns MutableStateFlow("192.168.1.1")
            coEvery { versionRepository.fetchVersion("192.168.1.1") } returns Result.failure(Exception("error"))
            val statusesFlow = createStatusFlow()

            val statuses = statusesFlow.take(2).toList()

            assertEquals(ConnectionBannerVmStatus.UNCHECKED, statuses[0])
            assertEquals(ConnectionBannerVmStatus.DISCONNECTED, statuses[1])
            coVerify(exactly = 1) { versionRepository.fetchVersion("192.168.1.1") }
            confirmVerified(versionRepository)
        }

    @Test
    fun `IPアドレスがnullからIPが設定されるとIP_NOT_CONFIGUREDからCONNECTEDへ遷移する`() =
        runTest(UnconfinedTestDispatcher()) {
            val ipFlow = MutableStateFlow<String?>(null)
            every { ipRepository.serverIp() } returns ipFlow
            coEvery { versionRepository.fetchVersion("192.168.1.1") } returns Result.success("1.0.0")
            val statusesFlow = createStatusFlow()
            val emitted = mutableListOf<ConnectionBannerVmStatus>()
            val job = launch { statusesFlow.collect { emitted.add(it) } }

            assertEquals(ConnectionBannerVmStatus.IP_NOT_CONFIGURED, emitted.last())

            ipFlow.update { "192.168.1.1" }

            assertEquals(ConnectionBannerVmStatus.CONNECTED, emitted.last())
            coVerify(exactly = 1) { versionRepository.fetchVersion("192.168.1.1") }
            confirmVerified(versionRepository)
            job.cancel()
        }

    protected abstract fun createStatusFlow(
        fetchServerVersion: FetchServerVersionUseCase,
        observeServerIp: ObserveServerIpUseCase,
    ): Flow<ConnectionBannerVmStatus>

    private fun createStatusFlow(): Flow<ConnectionBannerVmStatus> =
        createStatusFlow(
            fetchServerVersion = FetchServerVersionUseCase(versionRepository),
            observeServerIp = ObserveServerIpUseCase(ipRepository),
        )
}
