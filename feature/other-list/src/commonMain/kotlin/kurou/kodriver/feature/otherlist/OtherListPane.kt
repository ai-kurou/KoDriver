package kurou.kodriver.feature.otherlist

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.tooling.preview.Preview
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.ListRowFocusState
import kurou.kodriver.core.designsystem.LocalBottomEndOverlayInset
import kurou.kodriver.core.designsystem.RestoreRowFocusOnReturn
import kurou.kodriver.core.designsystem.ScrollToTopEffect
import kurou.kodriver.core.designsystem.handleListRowArrowKey

/** アプリバージョン行の項目キー。項目の [OtherListItemType.id] と衝突しない値を使う。 */
internal const val OTHER_APP_VERSION_ROW_KEY = "app_version"

/**
 * 一覧の行（項目とアプリバージョン行）の並び順と、LazyColumn 上のインデックス。
 *
 * [itemKeys] は ↑↓ の移動対象を並び順に並べた項目キー、[lazyIndexes] は同じ順の LazyList インデックス。
 */
internal data class OtherListRowLayout(
    val itemKeys: List<String>,
    val lazyIndexes: List<Int>,
)

/**
 * [OtherListPane] の LazyColumn に並べる順序を再現し、行の項目キーと LazyList インデックスを求める。
 *
 * セクションごとに見出し1件を並べ、項目は1件ずつ並べる（区切り線は同じ item の中にあり数えない）。
 * アプリバージョン行は情報セクションの項目の直後に置く。項目のないセクションは並べない。
 */
internal fun otherListRowLayout(
    groupedItems: Map<OtherListSection, List<OtherListItemType>>,
    appVersionVisible: Boolean,
): OtherListRowLayout {
    val itemKeys = mutableListOf<String>()
    val lazyIndexes = mutableListOf<Int>()
    var lazyIndex = 0
    otherListSections.forEach { section ->
        val sectionItems = groupedItems[section].orEmpty()
        if (sectionItems.isNotEmpty()) {
            // セクション見出し
            lazyIndex += 1
            sectionItems.forEach { item ->
                itemKeys += item.id
                lazyIndexes += lazyIndex
                lazyIndex += 1
            }
            if (section == OtherListSection.Information && appVersionVisible) {
                itemKeys += OTHER_APP_VERSION_ROW_KEY
                lazyIndexes += lazyIndex
                lazyIndex += 1
            }
        }
    }
    return OtherListRowLayout(itemKeys = itemKeys, lazyIndexes = lazyIndexes)
}

/**
 * OtherList の画面を表示する Composable。
 */
@Composable
public fun OtherListPane(
    uiState: OtherListUiState,
    onItemClick: (OtherListItemType) -> Unit,
    onOverlayVisibleChange: (Boolean) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onDynamicColorEnabledChange: (Boolean) -> Unit,
    onHapticFeedbackEnabledChange: (Boolean) -> Unit,
    onStartupEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onAppVersionTapped: () -> Unit = {},
    scrollToTopRequest: Int = 0,
    restoreFocusItemKey: String? = null,
    onFocusRestored: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val groupedItems = remember(uiState.items) { uiState.items.groupBy { it.section() } }
    val appVersionVisible = uiState.appVersionLabel.isNotBlank() && uiState.appVersion.isNotBlank()
    val rowLayout = remember(groupedItems, appVersionVisible) { otherListRowLayout(groupedItems, appVersionVisible) }
    val rowFocusState = remember { ListRowFocusState() }
    RestoreRowFocusOnReturn(
        restoreItemKey = restoreFocusItemKey,
        focusState = rowFocusState,
        onRestored = onFocusRestored,
        listState = listState,
        lazyIndexOf = { key ->
            val index = rowLayout.itemKeys.indexOf(key)
            if (index >= 0) rowLayout.lazyIndexes[index] else null
        },
    )

    ScrollToTopEffect(scrollToTopRequest = scrollToTopRequest) {
        listState.animateScrollToItem(0)
    }

    LazyColumn(
        state = listState,
        // 右下に重なる音声認識のチップに、末尾の項目が隠れないよう下端を空ける
        contentPadding = PaddingValues(bottom = LocalBottomEndOverlayInset.current),
        modifier =
            modifier
                .fillMaxSize()
                .onKeyEvent { event ->
                    handleListRowArrowKey(
                        event = event,
                        focusState = rowFocusState,
                        itemKeys = rowLayout.itemKeys,
                        firstItemLazyIndex = 0,
                        listState = listState,
                        coroutineScope = coroutineScope,
                        lazyIndexOf = { rowLayout.lazyIndexes[it] },
                    )
                }.padding(vertical = KoDriverSpacing.small),
    ) {
        otherListSections.forEach { section ->
            val sectionItems = groupedItems[section].orEmpty()
            if (sectionItems.isNotEmpty()) {
                item(key = "section_${section.name}") {
                    OtherListSectionHeader(section)
                    HorizontalDivider()
                }
                items(sectionItems, key = { it.id }) { item ->
                    OtherListItem(
                        item = item,
                        uiState = uiState,
                        onOverlayVisibleChange = onOverlayVisibleChange,
                        onKeepScreenOnChange = onKeepScreenOnChange,
                        onDynamicColorEnabledChange = onDynamicColorEnabledChange,
                        onHapticFeedbackEnabledChange = onHapticFeedbackEnabledChange,
                        onStartupEnabledChange = onStartupEnabledChange,
                        onItemClick = onItemClick,
                        modifier = Modifier.onFocusChanged { rowFocusState.onRowFocusChanged(item.id, it.hasFocus) },
                        rowFocusRequester = rowFocusState.requesterFor(item.id),
                    )
                    HorizontalDivider()
                }
                if (section == OtherListSection.Information && appVersionVisible) {
                    item(key = OTHER_APP_VERSION_ROW_KEY) {
                        OtherAppVersionListItem(
                            appVersionLabel = uiState.appVersionLabel,
                            appVersion = uiState.appVersion,
                            onAppVersionTapped = onAppVersionTapped,
                            modifier =
                                Modifier.onFocusChanged {
                                    rowFocusState.onRowFocusChanged(OTHER_APP_VERSION_ROW_KEY, it.hasFocus)
                                },
                            rowFocusRequester = rowFocusState.requesterFor(OTHER_APP_VERSION_ROW_KEY),
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherListPanePreview() {
    KoDriverTheme {
        OtherListPane(
            uiState = OtherListUiState(),
            onItemClick = {},
            onOverlayVisibleChange = {},
            onKeepScreenOnChange = {},
            onDynamicColorEnabledChange = {},
            onHapticFeedbackEnabledChange = {},
            onStartupEnabledChange = {},
        )
    }
}
