package kurou.kodriver.data

import kurou.kodriver.domain.model.AceWindowsReadoutCatalog
import kurou.kodriver.domain.model.Gt7Ps5ReadoutCatalog
import kurou.kodriver.domain.model.LmuWindowsReadoutCatalog
import kurou.kodriver.domain.model.ReadoutCatalog

/** 保存キーの復元順序は従来どおり LMU、GT7、ACE。 */
val APP_READOUT_CATALOG =
    ReadoutCatalog(listOf(LmuWindowsReadoutCatalog, Gt7Ps5ReadoutCatalog, AceWindowsReadoutCatalog))
