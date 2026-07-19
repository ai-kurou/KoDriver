package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.Gt7Ps5UdpPortPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SaveGt7Ps5UdpPortUseCaseTest {

    @Test
    fun `33740を保存できる`() = runBlocking {
        val repository = mockk<Gt7Ps5UdpPortPreferencesRepository>(relaxUnitFun = true)

        SaveGt7Ps5UdpPortUseCase(repository)(33740)

        coVerify(exactly = 1) { repository.savePort(33740) }
        confirmVerified(repository)
    }

    @Test
    fun `33741を保存できる`() = runBlocking {
        val repository = mockk<Gt7Ps5UdpPortPreferencesRepository>(relaxUnitFun = true)

        SaveGt7Ps5UdpPortUseCase(repository)(33741)

        coVerify(exactly = 1) { repository.savePort(33741) }
        confirmVerified(repository)
    }

    @Test
    fun `33740でも33741でもない値はIllegalArgumentExceptionをスローする`() = runBlocking {
        val repository = mockk<Gt7Ps5UdpPortPreferencesRepository>(relaxUnitFun = true)
        val useCase = SaveGt7Ps5UdpPortUseCase(repository)

        assertFailsWith<IllegalArgumentException> { useCase(33739) }
        assertFailsWith<IllegalArgumentException> { useCase(33742) }
        assertFailsWith<IllegalArgumentException> { useCase(0) }

        confirmVerified(repository)
    }
}
