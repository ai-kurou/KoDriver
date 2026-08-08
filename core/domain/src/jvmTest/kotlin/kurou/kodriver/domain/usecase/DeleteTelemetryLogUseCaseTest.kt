package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class DeleteTelemetryLogUseCaseTest {
    @MockK
    private lateinit var repository: TelemetryLogRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `指定したIDのログを削除する`() =
        runTest {
            coEvery { repository.deleteTelemetryLog(1L) } returns Unit
            val useCase = DeleteTelemetryLogUseCase(repository)

            useCase(1L)

            coVerify(exactly = 1) { repository.deleteTelemetryLog(1L) }
            confirmVerified(repository)
        }
}
