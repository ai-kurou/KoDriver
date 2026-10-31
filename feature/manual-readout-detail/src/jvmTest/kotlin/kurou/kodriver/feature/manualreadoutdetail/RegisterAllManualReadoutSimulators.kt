package kurou.kodriver.feature.manualreadoutdetail

import kurou.kodriver.domain.model.registerAceWindowsReadoutItemKeys
import kurou.kodriver.domain.model.registerGt7Ps5ReadoutItemKeys
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys

/** 手動読み上げの項目定義はレジストリ経由で引くため、テスト前に全シミュレーターを登録する。 */
internal fun registerAllManualReadoutSimulators() {
    registerLmuWindowsReadoutItemKeys()
    registerGt7Ps5ReadoutItemKeys()
    registerAceWindowsReadoutItemKeys()
}
