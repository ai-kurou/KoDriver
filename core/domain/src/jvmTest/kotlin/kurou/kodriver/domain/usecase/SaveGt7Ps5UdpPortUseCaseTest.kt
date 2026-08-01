package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.GT7_PS5_UDP_PORT_ALTERNATE
import kurou.kodriver.domain.model.GT7_PS5_UDP_PORT_DEFAULT
import kurou.kodriver.domain.repository.Gt7Ps5UdpPortPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SaveGt7Ps5UdpPortUseCaseTest {

    @MockK(relaxUnitFun = true)
    private lateinit var repository: Gt7Ps5UdpPortPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `33740を保存できる`() =
        runBlocking {
        SaveGt7Ps5UdpPortUseCase(repository)(GT7_PS5_UDP_PORT_DEFAULT)

        coVerify(exactly = 1) { repository.savePort(GT7_PS5_UDP_PORT_DEFAULT) }
        confirmVerified(repository)
    }

    @Test
    fun `33741を保存できる`() =
        runBlocking {
        SaveGt7Ps5UdpPortUseCase(repository)(GT7_PS5_UDP_PORT_ALTERNATE)

        coVerify(exactly = 1) { repository.savePort(GT7_PS5_UDP_PORT_ALTERNATE) }
        confirmVerified(repository)
    }

    @Test
    fun `33740でも33741でもない値はIllegalArgumentExceptionをスローする`() =
        runBlocking {
        val useCase = SaveGt7Ps5UdpPortUseCase(repository)

        assertFailsWith<IllegalArgumentException> { useCase(33739) }
        assertFailsWith<IllegalArgumentException> { useCase(33742) }
        assertFailsWith<IllegalArgumentException> { useCase(0) }

        confirmVerified(repository)
    }
}
