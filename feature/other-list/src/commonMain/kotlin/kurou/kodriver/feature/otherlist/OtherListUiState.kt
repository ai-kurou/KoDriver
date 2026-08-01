package kurou.kodriver.feature.otherlist

import kurou.kodriver.domain.model.DYNAMIC_COLOR_ENABLED_DEFAULT
import kurou.kodriver.domain.model.EXIT_CONFIRMATION_ENABLED_DEFAULT
import kurou.kodriver.domain.model.KEEP_SCREEN_ON_ENABLED_DEFAULT

/**
 * OtherList 画面の表示状態。
 */
data class OtherListUiState(
    val items: List<OtherListItemType> = buildOtherListItems(),
    val selectedItem: OtherListItemType? = null,
    val hasAppUpdate: Boolean = false,
    val keepScreenOn: Boolean = KEEP_SCREEN_ON_ENABLED_DEFAULT,
    val exitConfirmationEnabled: Boolean = EXIT_CONFIRMATION_ENABLED_DEFAULT,
    val dynamicColorEnabled: Boolean = DYNAMIC_COLOR_ENABLED_DEFAULT,
    val appVersionLabel: String = "",
    val appVersion: String = "",
)
