@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveLmuWindowsVehicleClassUseCaseTest {
    @MockK
    private lateinit var repo: LmuWindowsVehicleClassRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invoke はリポジトリの vehicleClassStream を返す`() =
        runTest {
            val expected = LmuWindowsVehicleClassData.fromRawValue("Hypercar")
            every { repo.vehicleClassStream() } returns flowOf(expected)
            val useCase = ObserveLmuWindowsVehicleClassUseCase(repo)

            val result = useCase().first()

            assertEquals(expected, result)
            verify(exactly = 1) { repo.vehicleClassStream() }
            confirmVerified(repo)
        }

    @Test
    fun `invoke は空のフローをそのまま返す`() =
        runTest {
            every { repo.vehicleClassStream() } returns flowOf()
            val useCase = ObserveLmuWindowsVehicleClassUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertTrue(results.isEmpty())
            verify(exactly = 1) { repo.vehicleClassStream() }
            confirmVerified(repo)
        }

    @Test
    fun `複数のデータを順番通りに流す`() =
        runTest {
            val data1 = LmuWindowsVehicleClassData.fromRawValue("LMP2")
            val data2 = LmuWindowsVehicleClassData.fromRawValue("GTE")
            val data3 = LmuWindowsVehicleClassData.fromRawValue("LMGT3")
            every { repo.vehicleClassStream() } returns flowOf(data1, data2, data3)
            val useCase = ObserveLmuWindowsVehicleClassUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertEquals(listOf(data1, data2, data3), results)
            verify(exactly = 1) { repo.vehicleClassStream() }
            confirmVerified(repo)
        }
}
