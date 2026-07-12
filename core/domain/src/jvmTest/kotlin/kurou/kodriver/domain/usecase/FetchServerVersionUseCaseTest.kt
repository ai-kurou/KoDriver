package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.ServerVersionRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun createServerVersionRepository(result: Result<String>): ServerVersionRepository {
    val repository = mockk<ServerVersionRepository>()
    coEvery { repository.fetchVersion(any()) } returns result
    return repository
}

class FetchServerVersionUseCaseTest {

    @Test
    fun `バージョン取得成功時にResultSuccessを返す`() = runBlocking {
        val useCase = FetchServerVersionUseCase(createServerVersionRepository(Result.success("1.2.3")))
        val result = useCase("192.168.1.1")
        assertEquals("1.2.3", result.getOrNull())
    }

    @Test
    fun `バージョン取得失敗時にResultFailureを返す`() = runBlocking {
        val useCase = FetchServerVersionUseCase(
            createServerVersionRepository(Result.failure(RuntimeException("network error"))),
        )
        val result = useCase("192.168.1.1")
        assertTrue(result.isFailure)
    }
}
