package kurou.kodriver.domain.usecase

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FetchServerVersionUseCaseTest {

    @Test
    fun `バージョン取得成功時にResultSuccessを返す`() = runBlocking {
        val useCase = FetchServerVersionUseCase(FakeServerVersionRepository(Result.success("1.2.3")))
        val result = useCase("192.168.1.1")
        assertEquals("1.2.3", result.getOrNull())
    }

    @Test
    fun `バージョン取得失敗時にResultFailureを返す`() = runBlocking {
        val useCase = FetchServerVersionUseCase(
            FakeServerVersionRepository(Result.failure(RuntimeException("network error"))),
        )
        val result = useCase("192.168.1.1")
        assertTrue(result.isFailure)
    }
}
