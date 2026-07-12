@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveLmuWindowsVehicleDamageUseCaseTest {

    @Test
    fun `invoke はリポジトリの vehicleDamageStream を返す`() = runBlocking {
        val expected = LmuWindowsVehicleDamageData(overheating = true, partDetached = false, lastImpactMagnitude = 12.3)
        val repo = mockk<LmuWindowsVehicleDamageRepository>()
        every { repo.vehicleDamageStream() } returns flowOf(expected)
        val useCase = ObserveLmuWindowsVehicleDamageUseCase(repo)

        val result = useCase().first()

        assertEquals(expected, result)
    }

    @Test
    fun `invoke は空のフローをそのまま返す`() = runBlocking {
        val repo = mockk<LmuWindowsVehicleDamageRepository>()
        every { repo.vehicleDamageStream() } returns flowOf()
        val useCase = ObserveLmuWindowsVehicleDamageUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertTrue(results.isEmpty())
    }

    @Test
    fun `複数のデータを順番通りに流す`() = runBlocking {
        val data1 = LmuWindowsVehicleDamageData(overheating = false, partDetached = false, lastImpactMagnitude = 0.0)
        val data2 = LmuWindowsVehicleDamageData(overheating = true, partDetached = false, lastImpactMagnitude = 5.0)
        val data3 = LmuWindowsVehicleDamageData(overheating = true, partDetached = true, lastImpactMagnitude = 20.0)
        val repo = mockk<LmuWindowsVehicleDamageRepository>()
        every { repo.vehicleDamageStream() } returns flowOf(data1, data2, data3)
        val useCase = ObserveLmuWindowsVehicleDamageUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertEquals(listOf(data1, data2, data3), results)
    }
}
