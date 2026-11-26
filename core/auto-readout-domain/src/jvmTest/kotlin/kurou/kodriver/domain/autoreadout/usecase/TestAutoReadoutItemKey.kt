package kurou.kodriver.domain.autoreadout.usecase

import kurou.kodriver.domain.model.ReadoutItemKey

internal enum class TestAutoReadoutItemKey(
    override val value: String,
) : ReadoutItemKey {
    Flag("flag"),
    MyBestLap("my_best_lap"),
    VehicleApproach("vehicle_approach"),
    VehicleDamage("vehicle_damage"),
}
