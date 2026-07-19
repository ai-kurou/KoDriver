package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.ServerVersionRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FetchServerVersionUseCaseTest {

    @Test
    fun `バージョン取得成功時にResultSuccessを返す`() = runBlocking {
        val repository = mockk<ServerVersionRepository>()
        coEvery { repository.fetchVersion("192.168.1.1") } returns Result.success("1.2.3")
        val useCase = FetchServerVersionUseCase(repository)

        val result = useCase("192.168.1.1")

        assertEquals("1.2.3", result.getOrNull())
        coVerify(exactly = 1) { repository.fetchVersion("192.168.1.1") }
        confirmVerified(repository)
    }

    @Test
    fun `バージョン取得失敗時にResultFailureを返す`() = runBlocking {
        val repository = mockk<ServerVersionRepository>()
        coEvery { repository.fetchVersion("192.168.1.1") } returns Result.failure(RuntimeException("network error"))
        val useCase = FetchServerVersionUseCase(repository)

        val result = useCase("192.168.1.1")

        assertTrue(result.isFailure)
        coVerify(exactly = 1) { repository.fetchVersion("192.168.1.1") }
        confirmVerified(repository)
    }
}
