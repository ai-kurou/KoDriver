package kurou.kodriver.feature.main

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.usecase.CheckAceWindowsConnectionUseCase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsBannerConnectionCheckerTest {
    @MockK
    private lateinit var repository: AceWindowsFuelRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `接続確認に成功するとCONNECTEDを返す`() =
        runTest {
            coEvery { repository.isConnected() } returns true
            val checker = AceWindowsBannerConnectionChecker(CheckAceWindowsConnectionUseCase(repository))

            val status = checker.statusFlow().first()

            assertEquals(ConnectionBannerVmStatus.CONNECTED, status)
            coVerify(exactly = 1) { repository.isConnected() }
            confirmVerified(repository)
        }

    @Test
    fun `接続確認に失敗するとDISCONNECTEDを返す`() =
        runTest {
            coEvery { repository.isConnected() } returns false
            val checker = AceWindowsBannerConnectionChecker(CheckAceWindowsConnectionUseCase(repository))

            val status = checker.statusFlow().first()

            assertEquals(ConnectionBannerVmStatus.DISCONNECTED, status)
            coVerify(exactly = 1) { repository.isConnected() }
            confirmVerified(repository)
        }

    @Test
    fun `接続確認で例外が発生してもDISCONNECTEDとして監視を継続する`() =
        runTest {
            coEvery { repository.isConnected() } throws RuntimeException("connection check failed") andThen true
            val checker = AceWindowsBannerConnectionChecker(CheckAceWindowsConnectionUseCase(repository))

            val statuses = checker.statusFlow().take(2).toList()

            assertEquals(ConnectionBannerVmStatus.DISCONNECTED, statuses[0])
            assertEquals(ConnectionBannerVmStatus.CONNECTED, statuses[1])
            coVerify(exactly = 2) { repository.isConnected() }
            confirmVerified(repository)
        }
}
