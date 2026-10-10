package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kurou.kodriver.domain.model.BrakeThicknessMeters
import kurou.kodriver.domain.model.LmuWindowsBrakeWearData
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
 * ブレーキ交換で厚さが増えれば最大値も更新される。REST の取得は車両クラスの流れに依存させず、車両クラスが変わったときは最大値を取り直す
 * （共有メモリは毎フレーム同じクラスを流すため、重複は除外して実際に変わったときだけ取り直す）。
 * このため、摩耗済みの状態で観測を始めると、その時点の厚さが 100% として扱われる。
 */
class ObserveLmuWindowsBrakeWearRemainingUseCase(
    private val brakeWearRepository: LmuWindowsBrakeWearRepository,
    private val vehicleClassRepository: LmuWindowsVehicleClassRepository,
) {
    operator fun invoke(): Flow<LmuWindowsBrakeWearRemainingData?> =
        flow {
            var vehicleClass: LmuWindowsVehicleClassData = LmuWindowsVehicleClassData.Unknown(raw = "")
            var maxThickness = emptyMap<WheelIndex, BrakeThicknessMeters>()
            merge(
                vehicleClassRepository
                    .vehicleClassStream()
                    .distinctUntilChanged()
                    .map<LmuWindowsVehicleClassData, Event> { Event.VehicleClass(it) },
                brakeWearRepository.brakeWearStream().map { Event.Wear(it) },
            ).collect { event ->
                when (event) {
                    is Event.VehicleClass -> {
                        // プレイヤー車両が Scoring から一時的に見つからない（空のクラス）間は、直前のクラスを保つ。
                        val isBlank = event.value is LmuWindowsVehicleClassData.Unknown && event.value.name.isEmpty()
                        if (!isBlank && event.value != vehicleClass) {
                            vehicleClass = event.value
                            maxThickness = emptyMap()
                        }
                    }

                    is Event.Wear -> {
                        val wear = event.value
                        if (wear == null) {
                            emit(null)
                        } else {
                            maxThickness =
                                wear.wheels.mapValues { (wheel, current) ->
                                    maxOf(current, maxThickness[wheel] ?: current)
                                }
                            val failureThickness = lmuWindowsVehicleClassBrakeFailureThicknessDefault(vehicleClass)
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
            }
        }

    private sealed interface Event {
        data class VehicleClass(
            val value: LmuWindowsVehicleClassData,
        ) : Event

        data class Wear(
            val value: LmuWindowsBrakeWearData?,
        ) : Event
    }
}
