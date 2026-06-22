@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.main

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
import kurou.kodriver.domain.repository.ServerIpRepository
import kurou.kodriver.domain.repository.ServerVersionRepository
import kurou.kodriver.domain.usecase.FetchServerVersionUseCase
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuServerBannerConnectionCheckerTest {

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
    }

    @Test
    fun `IPアドレスが設定されサーバー取得に失敗するとDISCONNECTEDを返す`() = runTest {
        val checker = createChecker(ip = "192.168.1.1", versionResult = Result.failure(Exception("error")))

        val statuses = checker.statusFlow().take(2).toList()

        assertEquals(ConnectionBannerVmStatus.UNCHECKED, statuses[0])
        assertEquals(ConnectionBannerVmStatus.DISCONNECTED, statuses[1])
    }

    @Test
    fun `IPアドレスがnullからIPが設定されるとIP_NOT_CONFIGUREDからCONNECTEDへ遷移する`() =
        runTest(UnconfinedTestDispatcher()) {
            val ipRepository = FakeServerIpRepository(initial = null)
            val checker = createChecker(ipRepository = ipRepository, versionResult = Result.success("1.0.0"))
            val emitted = mutableListOf<ConnectionBannerVmStatus>()
            val job = launch { checker.statusFlow().collect { emitted.add(it) } }

            assertEquals(ConnectionBannerVmStatus.IP_NOT_CONFIGURED, emitted.last())

            ipRepository.save("192.168.1.1")

            assertEquals(ConnectionBannerVmStatus.CONNECTED, emitted.last())
            job.cancel()
        }

    private fun createChecker(
        ip: String? = null,
        versionResult: Result<String> = Result.success("1.0.0"),
        ipRepository: FakeServerIpRepository = FakeServerIpRepository(ip),
    ) = LmuServerBannerConnectionChecker(
        fetchServerVersion = FetchServerVersionUseCase(FakeServerVersionRepository(versionResult)),
        observeServerIp = ObserveServerIpUseCase(ipRepository),
    )
}

private class FakeServerIpRepository(initial: String?) : ServerIpRepository {
    private val flow = MutableStateFlow(initial)
    override fun serverIp(): Flow<String?> = flow
    override suspend fun saveServerIp(ip: String) { flow.update { ip } }
    fun save(ip: String?) { flow.update { ip } }
}

private class FakeServerVersionRepository(
    private val result: Result<String>,
) : ServerVersionRepository {
    override suspend fun fetchVersion(ip: String): Result<String> = result
}
