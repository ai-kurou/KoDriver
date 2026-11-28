package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsNearbyVehiclesData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachRepository

/** 閾値を適用していない周辺車両の位置を購読する。サーバーからの配信に使う。 */
public class ObserveLmuWindowsNearbyVehiclesUseCase(
    private val repository: LmuWindowsVehicleApproachRepository,
) {
    public operator fun invoke(): Flow<LmuWindowsNearbyVehiclesData> = repository.nearbyVehiclesStream()
}
