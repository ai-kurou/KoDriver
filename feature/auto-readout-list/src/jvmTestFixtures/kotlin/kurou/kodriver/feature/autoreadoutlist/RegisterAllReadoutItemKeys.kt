package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.domain.model.registerAceWindowsReadoutItemKeys
import kurou.kodriver.domain.model.registerGt7Ps5ReadoutItemKeys
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys

/** 一覧画面は全シミュレーターを扱うため、テストでは3つの読み上げキーをすべて登録してから使う。 */
fun registerAllReadoutItemKeys() {
    registerLmuWindowsReadoutItemKeys()
    registerGt7Ps5ReadoutItemKeys()
    registerAceWindowsReadoutItemKeys()
}
