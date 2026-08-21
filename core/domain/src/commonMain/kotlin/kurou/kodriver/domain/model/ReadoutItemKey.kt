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

    sealed interface LmuWindows : ReadoutItemKey {
        sealed interface TopLevel :
            LmuWindows,
            ReadoutItemKey.TopLevel

        sealed interface VehicleApproach : LmuWindows {
            data object Root : VehicleApproach, TopLevel {
                override val value = "lmu_windows_vehicle_approach"
                override val supportsQueue = false
            }

            data object Sustained : VehicleApproach {
                override val value = "lmu_windows_vehicle_approach_sustained"
            }

            data object StartReadout : VehicleApproach {
                override val value = "lmu_windows_vehicle_approach_start_readout"
            }
        }

        sealed interface MyBestLap : LmuWindows {
            data object Root : MyBestLap, TopLevel {
                override val value = "lmu_windows_my_best_lap"
                override val supportsQueue = true
            }
        }

        sealed interface Flag : LmuWindows {
            data object Root : Flag, TopLevel {
                override val value = "lmu_windows_flag"
                override val supportsQueue = true
            }

            data object BlueFlag : Flag {
                override val value = "lmu_windows_blue_flag"
            }

            data object SectorYellowFlag : Flag {
                override val value = "lmu_windows_sector_yellow_flag"
            }

            data object FullCourseYellow : Flag {
                override val value = "lmu_windows_full_course_yellow"
            }

            data object RedFlag : Flag {
                override val value = "lmu_windows_red_flag"
            }
        }

        sealed interface VehicleDamage : LmuWindows {
            data object Root : VehicleDamage, TopLevel {
                override val value = "lmu_windows_vehicle_damage"
                override val supportsQueue = true
            }

            data object Overheat : VehicleDamage {
                override val value = "lmu_windows_overheat"
            }
        }

        sealed interface TyreTemperature : LmuWindows {
            data object Root : TyreTemperature, TopLevel {
                override val value = "lmu_windows_tyre_temperature"
                override val supportsQueue = true
            }

            data object OverheatWarning : TyreTemperature {
                override val value = "lmu_windows_tyre_temperature_overheat_warning"
            }

            data object LowWarning : TyreTemperature {
                override val value = "lmu_windows_tyre_temperature_low_warning"
            }
        }

        sealed interface PitTiming : LmuWindows {
            data object Root : PitTiming, TopLevel {
                override val value = "lmu_windows_pit_timing"
                override val supportsQueue = true
            }
        }

        sealed interface RemainingVirtualEnergy : LmuWindows {
            data object Root : RemainingVirtualEnergy, TopLevel {
                override val value = "lmu_windows_remaining_virtual_energy"
                override val supportsQueue = true
            }
        }

        sealed interface TyreWear : LmuWindows {
            data object Root : TyreWear, TopLevel {
                override val value = "lmu_windows_tyre_wear"
                override val supportsQueue = true
            }
        }
    }

    sealed interface Gt7Ps5 : ReadoutItemKey {
        sealed interface TopLevel :
            Gt7Ps5,
            ReadoutItemKey.TopLevel

        sealed interface MyBestLap : Gt7Ps5 {
            data object Root : MyBestLap, TopLevel {
                override val value = "gt7_ps5_my_best_lap"
                override val supportsQueue = true
            }
        }

        sealed interface RemainingFuelLaps : Gt7Ps5 {
            data object Root : RemainingFuelLaps, TopLevel {
                override val value = "gt7_ps5_remaining_fuel_laps"
                override val supportsQueue = true
            }
        }

        sealed interface RemainingFuel : Gt7Ps5 {
            data object Root : RemainingFuel, TopLevel {
                override val value = "gt7_ps5_remaining_fuel"
                override val supportsQueue = true
            }
        }

        sealed interface TyreTemperature : Gt7Ps5 {
            data object Root : TyreTemperature, TopLevel {
                override val value = "gt7_ps5_tyre_temperature"
                override val supportsQueue = true
            }

            data object OverheatWarning : TyreTemperature {
                override val value = "gt7_ps5_tyre_temperature_overheat_warning"
            }
        }
    }

    sealed interface AceWindows : ReadoutItemKey {
        sealed interface TopLevel :
            AceWindows,
            ReadoutItemKey.TopLevel

        sealed interface VehicleApproach : AceWindows {
            data object Root : VehicleApproach, TopLevel {
                override val value = "ace_windows_vehicle_approach"
                override val supportsQueue = false
            }
        }

        sealed interface Flag : AceWindows {
            data object Root : Flag, TopLevel {
                override val value = "ace_windows_flag"
                override val supportsQueue = true
            }

            data object WhiteFlag : Flag {
                override val value = "ace_windows_white_flag"
            }

            data object GreenFlag : Flag {
                override val value = "ace_windows_green_flag"
            }

            data object RedFlag : Flag {
                override val value = "ace_windows_red_flag"
            }

            data object BlueFlag : Flag {
                override val value = "ace_windows_blue_flag"
            }

            data object YellowFlag : Flag {
                override val value = "ace_windows_yellow_flag"
            }

            data object BlackFlag : Flag {
                override val value = "ace_windows_black_flag"
            }

            data object BlackWhiteFlag : Flag {
                override val value = "ace_windows_black_white_flag"
            }

            data object CheckeredFlag : Flag {
                override val value = "ace_windows_checkered_flag"
            }

            data object OrangeCircleFlag : Flag {
                override val value = "ace_windows_orange_circle_flag"
            }

            data object RedYellowStripesFlag : Flag {
                override val value = "ace_windows_red_yellow_stripes_flag"
            }
        }

        sealed interface RemainingFuel : AceWindows {
            data object Root : RemainingFuel, TopLevel {
                override val value = "ace_windows_remaining_fuel"
                override val supportsQueue = true
            }
        }

        sealed interface TyreTemperature : AceWindows {
            data object Root : TyreTemperature, TopLevel {
                override val value = "ace_windows_tyre_temperature"
                override val supportsQueue = true
            }

            data object OverheatWarning : TyreTemperature {
                override val value = "ace_windows_tyre_temperature_overheat_warning"
            }
        }
    }

    companion object {
        /** 既存保存値の復元対象となる全キー。新しいキーを追加した場合はここにも追加する。 */
        val entries by lazy {
            listOf(
                LmuWindows.VehicleApproach.Root,
                LmuWindows.VehicleApproach.Sustained,
                LmuWindows.VehicleApproach.StartReadout,
                LmuWindows.Flag.Root,
                LmuWindows.Flag.BlueFlag,
                LmuWindows.Flag.SectorYellowFlag,
                LmuWindows.Flag.FullCourseYellow,
                LmuWindows.Flag.RedFlag,
                LmuWindows.VehicleDamage.Root,
                LmuWindows.VehicleDamage.Overheat,
                LmuWindows.TyreTemperature.Root,
                LmuWindows.TyreTemperature.OverheatWarning,
                LmuWindows.TyreTemperature.LowWarning,
                LmuWindows.PitTiming.Root,
                LmuWindows.RemainingVirtualEnergy.Root,
                LmuWindows.TyreWear.Root,
                LmuWindows.MyBestLap.Root,
                Gt7Ps5.MyBestLap.Root,
                Gt7Ps5.RemainingFuelLaps.Root,
                Gt7Ps5.RemainingFuel.Root,
                Gt7Ps5.TyreTemperature.Root,
                Gt7Ps5.TyreTemperature.OverheatWarning,
                AceWindows.VehicleApproach.Root,
                AceWindows.Flag.Root,
                AceWindows.Flag.WhiteFlag,
                AceWindows.Flag.GreenFlag,
                AceWindows.Flag.RedFlag,
                AceWindows.Flag.BlueFlag,
                AceWindows.Flag.YellowFlag,
                AceWindows.Flag.BlackFlag,
                AceWindows.Flag.BlackWhiteFlag,
                AceWindows.Flag.CheckeredFlag,
                AceWindows.Flag.OrangeCircleFlag,
                AceWindows.Flag.RedYellowStripesFlag,
                AceWindows.RemainingFuel.Root,
                AceWindows.TyreTemperature.Root,
                AceWindows.TyreTemperature.OverheatWarning,
            )
        }

        /** DataStore に保存された [value] からキーを復元する。不明な値は null を返す。 */
        fun fromValue(value: String): ReadoutItemKey? = entries.find { it.value == value }
    }
}
