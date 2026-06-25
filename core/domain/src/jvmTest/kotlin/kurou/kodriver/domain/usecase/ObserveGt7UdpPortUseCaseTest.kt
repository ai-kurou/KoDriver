package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveGt7UdpPortUseCaseTest {

    @Test
    fun `初期値を返す・保存済みの値を返す`() = runBlocking {
        val repo = FakeGt7UdpPortPreferencesRepository(initial = 33740)
        val useCase = ObserveGt7UdpPortUseCase(repo)

        assertEquals(33740, useCase().first())

        repo.savePort(33741)
        assertEquals(33741, useCase().first())
    }
}
