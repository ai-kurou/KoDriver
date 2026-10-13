package kurou.kodriver.presentation

import androidx.compose.ui.unit.dp

/**
 * list/detail 構成の画面で一覧ペインの初期幅として使う 400.dp。
 *
 * app:shared は :core:* に依存できないため、:core:designsystem の KoDriverListPaneWidth と
 * 同値（400.dp）を別定義している。変更する場合は両方を同期させること。
 */
internal val AppPaneWidth = 400.dp
