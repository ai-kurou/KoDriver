package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SaveGt7Ps5UdpPortUseCaseTest {

    private val repo = FakeGt7Ps5UdpPortPreferencesRepository()
    private val useCase = SaveGt7Ps5UdpPortUseCase(repo)

    @Test
    fun `33740を保存できる`() = runBlocking {
        useCase(33740)
        assertEquals(33740, repo.port().first())
    }

    @Test
    fun `33741を保存できる`() = runBlocking {
        useCase(33741)
        assertEquals(33741, repo.port().first())
    }

    @Test
    fun `33740でも33741でもない値はIllegalArgumentExceptionをスローする`() = runBlocking<Unit> {
        assertFailsWith<IllegalArgumentException> { runBlocking { useCase(33739) } }
        assertFailsWith<IllegalArgumentException> { runBlocking { useCase(33742) } }
        assertFailsWith<IllegalArgumentException> { runBlocking { useCase(0) } }
    }
}
