@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.VehicleDamageData
import kurou.kodriver.domain.repository.VehicleDamageRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveVehicleDamageUseCaseTest {

    @Test
    fun `invoke はリポジトリの vehicleDamageStream を返す`() = runBlocking {
        val expected = VehicleDamageData(overheating = true, partDetached = false, lastImpactMagnitude = 12.3)
        val repo = FakeVehicleDamageRepository(stream = flowOf(expected))
        val useCase = ObserveVehicleDamageUseCase(repo)

        val result = useCase().first()

        assertEquals(expected, result)
    }

    @Test
    fun `invoke は空のフローをそのまま返す`() = runBlocking {
        val repo = FakeVehicleDamageRepository(stream = flowOf())
        val useCase = ObserveVehicleDamageUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertTrue(results.isEmpty())
    }

    @Test
    fun `複数のデータを順番通りに流す`() = runBlocking {
        val data1 = VehicleDamageData(overheating = false, partDetached = false, lastImpactMagnitude = 0.0)
        val data2 = VehicleDamageData(overheating = true, partDetached = false, lastImpactMagnitude = 5.0)
        val data3 = VehicleDamageData(overheating = true, partDetached = true, lastImpactMagnitude = 20.0)
        val repo = FakeVehicleDamageRepository(stream = flowOf(data1, data2, data3))
        val useCase = ObserveVehicleDamageUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertEquals(listOf(data1, data2, data3), results)
    }
}

private class FakeVehicleDamageRepository(
    private val stream: Flow<VehicleDamageData> = flowOf(),
) : VehicleDamageRepository {
    override fun vehicleDamageStream(): Flow<VehicleDamageData> = stream
}
