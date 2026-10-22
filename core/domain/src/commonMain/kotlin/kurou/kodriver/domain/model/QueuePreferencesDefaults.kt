package kurou.kodriver.domain.model

// listPane（AutoReadoutListViewModel）が参照するデフォルト値。各Simulatorのspecから合成する。
val QUEUE_ENABLED_STATE_DEFAULT: Map<ReadoutItemKey, Boolean>
    get() =
        ReadoutItemKeyRegistry.registeredSpecs.fold(emptyMap()) { result, spec ->
            result + spec.queueEnabledStateDefault
        }
