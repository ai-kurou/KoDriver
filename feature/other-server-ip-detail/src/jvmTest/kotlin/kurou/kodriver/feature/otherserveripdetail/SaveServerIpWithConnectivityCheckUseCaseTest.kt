package kurou.kodriver.feature.otherserveripdetail

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository
import kurou.kodriver.domain.usecase.SaveServerIpUseCase
import java.io.IOException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SaveServerIpWithConnectivityCheckUseCaseTest {
    @MockK
    private lateinit var repository: ServerIpPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    private fun createUseCase(
        reachable: Boolean = true,
        checkedIps: MutableList<String> = mutableListOf(),
    ) = SaveServerIpWithConnectivityCheckUseCase(
        validateServerIpAddress = ValidateServerIpAddressUseCase(),
        connectivityChecker =
            ServerConnectivityChecker { ip ->
                checkedIps += ip
                reachable
            },
        saveServerIp = SaveServerIpUseCase(repository),
    )

    @Test
    fun `接続可能な有効IPは保存する`() =
        runTest {
            coEvery { repository.saveServerIp("192.168.1.10") } returns Unit
            val checkedIps = mutableListOf<String>()

            val result = createUseCase(checkedIps = checkedIps)("192.168.1.10")

            assertEquals(SaveServerIpResult.Saved, result)
            assertEquals(listOf("192.168.1.10"), checkedIps)
            coVerify(exactly = 1) { repository.saveServerIp("192.168.1.10") }
            confirmVerified(repository)
        }

    @Test
    fun `不正なIPは接続確認も保存もしない`() =
        runTest {
            val checkedIps = mutableListOf<String>()

            val result = createUseCase(checkedIps = checkedIps)("invalid")

            assertEquals(SaveServerIpResult.InvalidIp, result)
            assertEquals(emptyList(), checkedIps)
            confirmVerified(repository)
        }

    @Test
    fun `接続できないIPは保存しない`() =
        runTest {
            val result = createUseCase(reachable = false)("192.168.1.10")

            assertEquals(SaveServerIpResult.Unreachable, result)
            confirmVerified(repository)
        }

    @Test
    fun `強制保存では接続確認を行わない`() =
        runTest {
            coEvery { repository.saveServerIp("192.168.1.10") } returns Unit
            val checkedIps = mutableListOf<String>()

            val result =
                createUseCase(reachable = false, checkedIps = checkedIps)(
                    ip = "192.168.1.10",
                    checkConnectivity = false,
                )

            assertEquals(SaveServerIpResult.Saved, result)
            assertEquals(emptyList(), checkedIps)
            coVerify(exactly = 1) { repository.saveServerIp("192.168.1.10") }
            confirmVerified(repository)
        }

    @Test
    fun `保存例外はSaveFailedを返す`() =
        runTest {
            coEvery { repository.saveServerIp("192.168.1.10") } throws IOException("保存失敗")

            val result = createUseCase()("192.168.1.10")

            assertEquals(SaveServerIpResult.SaveFailed, result)
            coVerify(exactly = 1) { repository.saveServerIp("192.168.1.10") }
            confirmVerified(repository)
        }

    @Test
    fun `保存中のキャンセルは再送出する`() =
        runTest {
            coEvery { repository.saveServerIp("192.168.1.10") } throws CancellationException("cancelled")

            assertFailsWith<CancellationException> {
                createUseCase()("192.168.1.10")
            }
            coVerify(exactly = 1) { repository.saveServerIp("192.168.1.10") }
            confirmVerified(repository)
        }
}
