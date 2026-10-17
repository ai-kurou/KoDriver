package kurou.kodriver.domain.model

import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_QUEUE_ENABLED_STATE_DEFAULT

// listPane（AutoReadoutListViewModel）が参照するデフォルト値。
// supportsQueue が true の ReadoutItemKey.TopLevel は必ずここに列挙すること（省略＝デフォルトfalse、ではない）。
val QUEUE_ENABLED_STATE_DEFAULT: Map<ReadoutItemKey, Boolean> =
    LMU_WINDOWS_QUEUE_ENABLED_STATE_DEFAULT +
        GT7_PS5_QUEUE_ENABLED_STATE_DEFAULT +
        ACE_WINDOWS_QUEUE_ENABLED_STATE_DEFAULT
