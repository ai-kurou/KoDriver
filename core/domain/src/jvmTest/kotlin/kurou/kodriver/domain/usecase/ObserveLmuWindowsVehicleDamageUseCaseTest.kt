@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveLmuWindowsVehicleDamageUseCaseTest {

    @MockK
    private lateinit var repo: LmuWindowsVehicleDamageRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invoke はリポジトリの vehicleDamageStream を返す`() = runBlocking {
        val expected = LmuWindowsVehicleDamageData(overheating = true, partDetached = false, lastImpactMagnitude = 12.3)
        every { repo.vehicleDamageStream() } returns flowOf(expected)
        val useCase = ObserveLmuWindowsVehicleDamageUseCase(repo)

        val result = useCase().first()

        assertEquals(expected, result)
        verify(exactly = 1) { repo.vehicleDamageStream() }
        confirmVerified(repo)
    }

    @Test
    fun `invoke は空のフローをそのまま返す`() = runBlocking {
        every { repo.vehicleDamageStream() } returns flowOf()
        val useCase = ObserveLmuWindowsVehicleDamageUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertTrue(results.isEmpty())
        verify(exactly = 1) { repo.vehicleDamageStream() }
        confirmVerified(repo)
    }

    @Test
    fun `複数のデータを順番通りに流す`() = runBlocking {
        val data1 = LmuWindowsVehicleDamageData(overheating = false, partDetached = false, lastImpactMagnitude = 0.0)
        val data2 = LmuWindowsVehicleDamageData(overheating = true, partDetached = false, lastImpactMagnitude = 5.0)
        val data3 = LmuWindowsVehicleDamageData(overheating = true, partDetached = true, lastImpactMagnitude = 20.0)
        every { repo.vehicleDamageStream() } returns flowOf(data1, data2, data3)
        val useCase = ObserveLmuWindowsVehicleDamageUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertEquals(listOf(data1, data2, data3), results)
        verify(exactly = 1) { repo.vehicleDamageStream() }
        confirmVerified(repo)
    }
}
