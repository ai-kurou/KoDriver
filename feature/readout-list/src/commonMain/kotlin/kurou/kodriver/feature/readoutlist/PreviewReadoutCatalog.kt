package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.AceWindowsReadoutCatalog
import kurou.kodriver.domain.model.Gt7Ps5ReadoutCatalog
import kurou.kodriver.domain.model.LmuWindowsReadoutCatalog
import kurou.kodriver.domain.model.ReadoutCatalog

/** Preview のサンプル表示用カタログ。実画面は Koin で注入する。 */
internal val previewReadoutCatalog =
    ReadoutCatalog(listOf(LmuWindowsReadoutCatalog, Gt7Ps5ReadoutCatalog, AceWindowsReadoutCatalog))
