package kurou.kodriver.data

import kurou.kodriver.domain.model.registerAceWindowsReadoutItemKeys
import kurou.kodriver.domain.model.registerGt7Ps5ReadoutItemKeys
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys

/** Registers all simulator readout catalogs before the application starts resolving preferences. */
public fun registerReadoutItemKeys() {
    registerLmuWindowsReadoutItemKeys()
    registerGt7Ps5ReadoutItemKeys()
    registerAceWindowsReadoutItemKeys()
}
