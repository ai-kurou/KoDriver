package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.AceWindowsReadoutCatalog
import kurou.kodriver.domain.model.Gt7Ps5ReadoutCatalog
import kurou.kodriver.domain.model.LmuWindowsReadoutCatalog
import kurou.kodriver.domain.model.ReadoutCatalog

internal val testReadoutCatalog =
    ReadoutCatalog(listOf(LmuWindowsReadoutCatalog, Gt7Ps5ReadoutCatalog, AceWindowsReadoutCatalog))
