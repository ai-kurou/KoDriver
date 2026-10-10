package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.BrakeThicknessMeters
import kurou.kodriver.domain.model.LmuWindowsBrakeWearData
import kurou.kodriver.domain.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.LmuWindowsBrakeWearRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassRepository
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsBrakeWearRemainingUseCaseTest {
    private val wearRepo: LmuWindowsBrakeWearRepository = mockk()
    private val classRepo: LmuWindowsVehicleClassRepository = mockk()

    private fun wear(
        frontLeft: Float,
        rearRight: Float = frontLeft,
    ) = LmuWindowsBrakeWearData(
        wheels =
            mapOf(
                WheelIndex.FRONT_LEFT to BrakeThicknessMeters(frontLeft),
                WheelIndex.FRONT_RIGHT to BrakeThicknessMeters(frontLeft),
                WheelIndex.REAR_LEFT to BrakeThicknessMeters(rearRight),
                WheelIndex.REAR_RIGHT to BrakeThicknessMeters(rearRight),
            ),
    )

    private fun LmuWindowsBrakeWearRemainingData?.frontLeftPercent(): Int? =
        this
            ?.wheels
            ?.getValue(WheelIndex.FRONT_LEFT)
            ?.remainingPercent
            ?.roundToInt()

    @Test
    fun `観測した最大の厚さを基準に残量%を流す`() =
        runTest {
            every { wearRepo.brakeWearStream() } returns flowOf(wear(0.036f), wear(0.0305f), wear(0.025f))
            every { classRepo.vehicleClassStream() } returns flowOf(LmuWindowsVehicleClassData.Hypercar)
            val useCase = ObserveLmuWindowsBrakeWearRemainingUseCase(wearRepo, classRepo)

            val results = buildList { useCase().collect { add(it?.wheels?.getValue(WheelIndex.FRONT_LEFT)) } }

            assertEquals(listOf(100, 50, 0), results.map { it?.remainingPercent?.roundToInt() })
            assertEquals(BrakeThicknessMeters(0.0305f), results[1]?.thickness)
            verify(exactly = 1) { wearRepo.brakeWearStream() }
            verify(exactly = 1) { classRepo.vehicleClassStream() }
            confirmVerified(wearRepo, classRepo)
        }

    @Test
    fun `ブレーキ交換で厚さが増えると最大値が更新され100%に戻る`() =
        runTest {
            every { wearRepo.brakeWearStream() } returns flowOf(wear(0.030f), wear(0.0275f), wear(0.036f))
            every { classRepo.vehicleClassStream() } returns flowOf(LmuWindowsVehicleClassData.Hypercar)
            val useCase = ObserveLmuWindowsBrakeWearRemainingUseCase(wearRepo, classRepo)

            val results = buildList { useCase().collect { add(it?.wheels?.getValue(WheelIndex.FRONT_LEFT)) } }

            assertEquals(listOf(100, 50, 100), results.map { it?.remainingPercent?.roundToInt() })
            verify(exactly = 1) { wearRepo.brakeWearStream() }
            verify(exactly = 1) { classRepo.vehicleClassStream() }
            confirmVerified(wearRepo, classRepo)
        }

    @Test
    fun `前後輪の最大値はホイールごとに別々に追跡する`() =
        runTest {
            every { wearRepo.brakeWearStream() } returns
                flowOf(wear(frontLeft = 0.036f, rearRight = 0.032f), wear(frontLeft = 0.0305f, rearRight = 0.0285f))
            every { classRepo.vehicleClassStream() } returns flowOf(LmuWindowsVehicleClassData.Hypercar)
            val useCase = ObserveLmuWindowsBrakeWearRemainingUseCase(wearRepo, classRepo)

            val last = buildList { useCase().collect { add(it) } }.last()

            assertEquals(
                50,
                last
                    ?.wheels
                    ?.getValue(WheelIndex.FRONT_LEFT)
                    ?.remainingPercent
                    ?.roundToInt(),
            )
            assertEquals(
                50,
                last
                    ?.wheels
                    ?.getValue(WheelIndex.REAR_RIGHT)
                    ?.remainingPercent
                    ?.roundToInt(),
            )
            verify(exactly = 1) { wearRepo.brakeWearStream() }
            verify(exactly = 1) { classRepo.vehicleClassStream() }
            confirmVerified(wearRepo, classRepo)
        }

    @Test
    fun `GT3は破損厚さ30mmで計算する`() =
        runTest {
            every { wearRepo.brakeWearStream() } returns flowOf(wear(0.036f), wear(0.033f))
            every { classRepo.vehicleClassStream() } returns flowOf(LmuWindowsVehicleClassData.Gt3)
            val useCase = ObserveLmuWindowsBrakeWearRemainingUseCase(wearRepo, classRepo)

            val last = buildList { useCase().collect { add(it) } }.last()

            assertEquals(
                50,
                last
                    ?.wheels
                    ?.getValue(WheelIndex.FRONT_LEFT)
                    ?.remainingPercent
                    ?.roundToInt(),
            )
            verify(exactly = 1) { wearRepo.brakeWearStream() }
            verify(exactly = 1) { classRepo.vehicleClassStream() }
            confirmVerified(wearRepo, classRepo)
        }

    @Test
    fun `車両クラスが変わると最大値を取り直す`() =
        runTest {
            val classes = MutableSharedFlow<LmuWindowsVehicleClassData>()
            val wears = MutableSharedFlow<LmuWindowsBrakeWearData?>()
            every { wearRepo.brakeWearStream() } returns wears
            every { classRepo.vehicleClassStream() } returns classes
            val useCase = ObserveLmuWindowsBrakeWearRemainingUseCase(wearRepo, classRepo)
            val results = mutableListOf<Int?>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                useCase().collect { results.add(it.frontLeftPercent()) }
            }

            classes.emit(LmuWindowsVehicleClassData.Hypercar)
            wears.emit(wear(0.036f))
            wears.emit(wear(0.0305f))
            classes.emit(LmuWindowsVehicleClassData.Gt3)
            wears.emit(wear(0.0305f))

            assertEquals(listOf<Int?>(100, 50, 100), results)
            verify(exactly = 1) { wearRepo.brakeWearStream() }
            verify(exactly = 1) { classRepo.vehicleClassStream() }
            confirmVerified(wearRepo, classRepo)
        }

    @Test
    fun `同じ車両クラスが繰り返し流れても最大値を取り直さない`() =
        runTest {
            val classes = MutableSharedFlow<LmuWindowsVehicleClassData>()
            val wears = MutableSharedFlow<LmuWindowsBrakeWearData?>()
            every { wearRepo.brakeWearStream() } returns wears
            every { classRepo.vehicleClassStream() } returns classes
            val useCase = ObserveLmuWindowsBrakeWearRemainingUseCase(wearRepo, classRepo)
            val results = mutableListOf<Int?>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                useCase().collect { results.add(it.frontLeftPercent()) }
            }

            classes.emit(LmuWindowsVehicleClassData.Hypercar)
            wears.emit(wear(0.036f))
            classes.emit(LmuWindowsVehicleClassData.Hypercar)
            wears.emit(wear(0.0305f))

            assertEquals(listOf<Int?>(100, 50), results)
            verify(exactly = 1) { wearRepo.brakeWearStream() }
            verify(exactly = 1) { classRepo.vehicleClassStream() }
            confirmVerified(wearRepo, classRepo)
        }

    @Test
    fun `取得できなかった周期はnullを流し最大値は保持する`() =
        runTest {
            every { wearRepo.brakeWearStream() } returns flowOf(wear(0.036f), null, wear(0.0305f))
            every { classRepo.vehicleClassStream() } returns flowOf(LmuWindowsVehicleClassData.Hypercar)
            val useCase = ObserveLmuWindowsBrakeWearRemainingUseCase(wearRepo, classRepo)

            val results =
                buildList { useCase().collect { add(it.frontLeftPercent()) } }

            assertEquals(listOf(100, null, 50), results)
            verify(exactly = 1) { wearRepo.brakeWearStream() }
            verify(exactly = 1) { classRepo.vehicleClassStream() }
            confirmVerified(wearRepo, classRepo)
        }

    @Test
    fun `車両クラスが流れてこなくても残量を流す`() =
        runTest {
            every { wearRepo.brakeWearStream() } returns flowOf(wear(0.036f), wear(0.0305f))
            every { classRepo.vehicleClassStream() } returns emptyFlow()
            val useCase = ObserveLmuWindowsBrakeWearRemainingUseCase(wearRepo, classRepo)

            val results =
                buildList { useCase().collect { add(it.frontLeftPercent()) } }

            assertEquals(listOf(100, 50), results)
            verify(exactly = 1) { wearRepo.brakeWearStream() }
            verify(exactly = 1) { classRepo.vehicleClassStream() }
            confirmVerified(wearRepo, classRepo)
        }

    @Test
    fun `空のクラスが一時的に流れても最大値を取り直さない`() =
        runTest {
            val classes = MutableSharedFlow<LmuWindowsVehicleClassData>()
            val wears = MutableSharedFlow<LmuWindowsBrakeWearData?>()
            every { wearRepo.brakeWearStream() } returns wears
            every { classRepo.vehicleClassStream() } returns classes
            val useCase = ObserveLmuWindowsBrakeWearRemainingUseCase(wearRepo, classRepo)
            val results = mutableListOf<Int?>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                useCase().collect { results.add(it.frontLeftPercent()) }
            }

            classes.emit(LmuWindowsVehicleClassData.Hypercar)
            wears.emit(wear(0.036f))
            classes.emit(LmuWindowsVehicleClassData.fromRawValue(""))
            wears.emit(wear(0.0305f))

            assertEquals(listOf<Int?>(100, 50), results)
            verify(exactly = 1) { wearRepo.brakeWearStream() }
            verify(exactly = 1) { classRepo.vehicleClassStream() }
            confirmVerified(wearRepo, classRepo)
        }
}
