package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRemainingFuelData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRepository

private const val LMU_WINDOWS_FUEL_PERCENTAGE_SCALE = 100.0
private const val LMU_WINDOWS_FUEL_PERCENTAGE_MAX = 100

/** 燃料容量が取得できない（0以下の）テレメトリは流さない。Android のダミー値もここで除外される。 */
class ObserveLmuWindowsFuelUseCase(
    private val repository: LmuWindowsRepository,
) {
    operator fun invoke(): Flow<LmuWindowsRemainingFuelData> =
        repository
            .telemetryStream()
            .map { it.fuel }
            .filter { it.capacityLiters.value > 0.0 }
            .map { fuel ->
                val percent = fuel.currentLiters.value / fuel.capacityLiters.value * LMU_WINDOWS_FUEL_PERCENTAGE_SCALE
                LmuWindowsRemainingFuelData(percent.toInt().coerceIn(0, LMU_WINDOWS_FUEL_PERCENTAGE_MAX))
            }
}
