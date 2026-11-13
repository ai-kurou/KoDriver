package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kurou.kodriver.core.designsystem.KoDriverExtendedColors
import kurou.kodriver.domain.model.ManualReadoutItemCategory

/** 手動読み上げ項目のカテゴリ色。手動タブ一覧・音声タブ・自動タブと同じ色にそろえる。 */
@Composable
internal fun manualReadoutCategoryColor(category: ManualReadoutItemCategory): Color {
    val colors = KoDriverExtendedColors.current
    return when (category) {
        ManualReadoutItemCategory.VIRTUAL_ENERGY -> colors.categoryVirtualEnergy
        ManualReadoutItemCategory.FUEL -> colors.categoryFuel
        ManualReadoutItemCategory.TYRE -> colors.categoryTyre
        ManualReadoutItemCategory.BRAKE -> colors.categoryBrake
        ManualReadoutItemCategory.LAP -> colors.categoryLap
    }
}
