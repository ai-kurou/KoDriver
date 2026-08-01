package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsTyreWearPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsTyreWearThresholdPercentageUseCaseTest {
    @MockK
    private lateinit var repo: LmuWindowsTyreWearPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値を返す・保存済みの値を返す`() =
        runBlocking {
            val state = MutableStateFlow(50)
            every { repo.observeThresholdPercentage() } returns state
            coEvery { repo.saveThresholdPercentage(30) } answers { state.update { 30 } }
            val useCase = ObserveLmuWindowsTyreWearThresholdPercentageUseCase(repo)

            assertEquals(50, useCase().first())

            repo.saveThresholdPercentage(30)
            assertEquals(30, useCase().first())

            verify(exactly = 2) { repo.observeThresholdPercentage() }
            coVerify(exactly = 1) { repo.saveThresholdPercentage(30) }
            confirmVerified(repo)
        }
}
