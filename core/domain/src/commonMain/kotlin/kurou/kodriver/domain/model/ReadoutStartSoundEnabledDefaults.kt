package kurou.kodriver.domain.model

// auto-readout-list（AutoReadoutListViewModel）が参照するデフォルト値。
// 車両接近などの個別設定は各Simulatorのspecで定義する。
val READOUT_START_SOUND_ENABLED_STATE_DEFAULT: Map<ReadoutItemKey, Boolean>
    get() =
        ReadoutItemKeyRegistry.registeredSpecs.fold(emptyMap()) { result, spec ->
            result + spec.startSoundEnabledStateDefault
        }
