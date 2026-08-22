package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObserveLmuWindowsConnectionUseCaseTest {
    @MockK
    private lateinit var repository: LmuWindowsRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `接続確認結果を返す`() =
        runTest {
            coEvery { repository.isConnected() } returns true
            val useCase = createUseCase(repository)

            val isConnected = withTimeout(1_000L) { useCase().first() }

            assertTrue(isConnected)
            coVerify(exactly = 1) { repository.isConnected() }
            confirmVerified(repository)
        }

    @Test
    fun `接続確認で例外が発生した場合は未接続として監視を継続する`() =
        runTest {
            coEvery { repository.isConnected() } throws RuntimeException("connection check failed") andThen true
            val useCase = createUseCase(repository)

            val firstIsConnected = withTimeout(5_000L) { useCase().first() }
            assertFalse(firstIsConnected)

            val connectedIsConnected = withTimeout(5_000L) { useCase().first { it } }
            assertTrue(connectedIsConnected)

            coVerify(exactly = 2) { repository.isConnected() }
            confirmVerified(repository)
        }

    private fun createUseCase(repository: LmuWindowsRepository) =
        ObserveLmuWindowsConnectionUseCase(
            checkLmuWindowsConnection = CheckLmuWindowsConnectionUseCase(repository),
        )
}
