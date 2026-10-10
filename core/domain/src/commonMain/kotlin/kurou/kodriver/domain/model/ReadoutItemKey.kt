package kurou.kodriver.domain.model

/**
 * 読み上げ機能を識別する永続化キー。
 *
 * [value] は DataStore の設定キー、読み上げ一覧の並び順、キュー可否判定で共有する安定値。
 * 既存値を変更すると保存済み設定が失われるため、表示文言の変更とは独立して扱う。
 *
 * `Root` は一覧画面のトップレベル項目、その他のキーは詳細画面内のサブ項目を表す。
 */
sealed interface ReadoutItemKey {
    /** DataStore に保存する安定識別子。 */
    val value: String

    /**
     * listPane のトップレベル項目（Root）であることを表すマーカー。
     * キューへ積んで後で読み上げてよいかどうかは Root 単位でのみ判定するため、
     * supportsQueue は TopLevel にのみ存在し、サブ項目には存在しない。
     * デフォルト値は持たせず、新規追加時に必ず true/false を明示させる。
     */
    sealed interface TopLevel : ReadoutItemKey {
        val supportsQueue: Boolean
    }

    companion object {
        /** 既存保存値の復元対象となる全キー。新しいキーを追加した場合はここにも追加する。 */
        val entries by lazy {
            listOf(
                LmuWindowsReadoutItemKey.VehicleApproach.Root,
                LmuWindowsReadoutItemKey.VehicleApproach.Sustained,
                LmuWindowsReadoutItemKey.VehicleApproach.StartReadout,
                LmuWindowsReadoutItemKey.Flag.Root,
                LmuWindowsReadoutItemKey.Flag.BlueFlag,
                LmuWindowsReadoutItemKey.Flag.SectorYellowFlag,
                LmuWindowsReadoutItemKey.Flag.FullCourseYellow,
                LmuWindowsReadoutItemKey.Flag.RedFlag,
                LmuWindowsReadoutItemKey.VehicleDamage.Root,
                LmuWindowsReadoutItemKey.VehicleDamage.Overheat,
                LmuWindowsReadoutItemKey.VehicleDamage.PartDetached,
                LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached,
                LmuWindowsReadoutItemKey.TyreTemperature.Root,
                LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning,
                LmuWindowsReadoutItemKey.TyreTemperature.LowWarning,
                LmuWindowsReadoutItemKey.PitTiming.Root,
                LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy,
                LmuWindowsReadoutItemKey.PitTiming.TyreWear,
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.WarningReadout,
                LmuWindowsReadoutItemKey.TyreWear.Root,
                LmuWindowsReadoutItemKey.TyreWear.WarningReadout,
                LmuWindowsReadoutItemKey.BrakeTemperature.Root,
                LmuWindowsReadoutItemKey.BrakeTemperature.WarningReadout,
                LmuWindowsReadoutItemKey.BrakeWear.Root,
                LmuWindowsReadoutItemKey.BrakeWear.WarningReadout,
                LmuWindowsReadoutItemKey.MyBestLap.Root,
                LmuWindowsReadoutItemKey.MyBestLap.DetailEnabled,
                Gt7Ps5ReadoutItemKey.MyBestLap.Root,
                Gt7Ps5ReadoutItemKey.MyBestLap.DetailEnabled,
                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.DetailEnabled,
                Gt7Ps5ReadoutItemKey.RemainingFuel.Root,
                Gt7Ps5ReadoutItemKey.RemainingFuel.DetailEnabled,
                Gt7Ps5ReadoutItemKey.TyreTemperature.Root,
                Gt7Ps5ReadoutItemKey.TyreTemperature.OverheatWarning,
                AceWindowsReadoutItemKey.VehicleApproach.Root,
                AceWindowsReadoutItemKey.VehicleApproach.StartReadout,
                AceWindowsReadoutItemKey.Flag.Root,
                AceWindowsReadoutItemKey.Flag.WhiteFlag,
                AceWindowsReadoutItemKey.Flag.GreenFlag,
                AceWindowsReadoutItemKey.Flag.RedFlag,
                AceWindowsReadoutItemKey.Flag.BlueFlag,
                AceWindowsReadoutItemKey.Flag.YellowFlag,
                AceWindowsReadoutItemKey.Flag.BlackFlag,
                AceWindowsReadoutItemKey.Flag.BlackWhiteFlag,
                AceWindowsReadoutItemKey.Flag.CheckeredFlag,
                AceWindowsReadoutItemKey.Flag.OrangeCircleFlag,
                AceWindowsReadoutItemKey.Flag.RedYellowStripesFlag,
                AceWindowsReadoutItemKey.RemainingFuel.Root,
                AceWindowsReadoutItemKey.RemainingFuel.DetailEnabled,
                AceWindowsReadoutItemKey.RemainingFuelLaps.Root,
                AceWindowsReadoutItemKey.RemainingFuelLaps.DetailEnabled,
                AceWindowsReadoutItemKey.TyreTemperature.Root,
                AceWindowsReadoutItemKey.TyreTemperature.OverheatWarning,
                AceWindowsReadoutItemKey.MyBestLap.Root,
                AceWindowsReadoutItemKey.MyBestLap.DetailEnabled,
            )
        }

        /** DataStore に保存された [value] からキーを復元する。不明な値は null を返す。 */
        fun fromValue(value: String): ReadoutItemKey? = entries.find { it.value == value }
    }
}
