package kurou.kodriver.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kurou.kodriver.domain.model.BrakeThicknessMeters
import kurou.kodriver.domain.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.model.LmuWindowsBrakeWearWheelRemaining
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.model.calculateBrakeWearRemainingPercent
import kurou.kodriver.domain.model.lmuWindowsVehicleClassBrakeFailureThicknessDefault
import kurou.kodriver.domain.repository.LmuWindowsBrakeWearRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassRepository

/**
 * ブレーキの残量（厚さと%）を流す。取得できなかった周期は null を流す。
 *
 * REST API は新品時の厚さを返さないため、観測した最大の厚さを新品時の厚さとみなす。
 * ブレーキ交換で厚さが増えれば最大値も更新される。車両クラスが変わったときは最大値を取り直す
 * （共有メモリは毎フレーム同じクラスを流すため、重複は除外して実際に変わったときだけ取り直す）。
 * このため、摩耗済みの状態で観測を始めると、その時点の厚さが 100% として扱われる。
 */
class ObserveLmuWindowsBrakeWearRemainingUseCase(
    private val brakeWearRepository: LmuWindowsBrakeWearRepository,
    private val vehicleClassRepository: LmuWindowsVehicleClassRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<LmuWindowsBrakeWearRemainingData?> =
        vehicleClassRepository
            .vehicleClassStream()
            .distinctUntilChanged()
            .flatMapLatest { vehicleClass -> remainingStream(vehicleClass) }

    private fun remainingStream(vehicleClass: LmuWindowsVehicleClassData): Flow<LmuWindowsBrakeWearRemainingData?> =
        flow {
            val failureThickness = lmuWindowsVehicleClassBrakeFailureThicknessDefault(vehicleClass)
            var maxThickness = emptyMap<WheelIndex, BrakeThicknessMeters>()
            brakeWearRepository.brakeWearStream().collect { wear ->
                if (wear == null) {
                    emit(null)
                    return@collect
                }
                maxThickness =
                    wear.wheels.mapValues { (wheel, current) ->
                        maxOf(current, maxThickness[wheel] ?: current)
                    }
                emit(
                    LmuWindowsBrakeWearRemainingData(
                        wheels =
                            wear.wheels.mapValues { (wheel, current) ->
                                LmuWindowsBrakeWearWheelRemaining(
                                    thickness = current,
                                    remainingPercent =
                                        calculateBrakeWearRemainingPercent(
                                            current = current,
                                            maxThickness = maxThickness.getValue(wheel),
                                            failureThickness = failureThickness,
                                        ),
                                )
                            },
                    ),
                )
            }
        }
}
