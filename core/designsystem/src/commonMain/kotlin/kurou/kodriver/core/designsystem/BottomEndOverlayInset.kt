package kurou.kodriver.core.designsystem

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * アプリ共通シェルが各画面の右下に重ねている要素（音声認識のチップ）が占める高さと幅。
 *
 * 画面側が右下に置く要素（FAB・Snackbar など）は、この分だけ上または端から避けて、重ならないようにする。
 * 提供されない場合（プレビューやテスト）は何も重ねていないため 0 になる。
 */
val LocalBottomEndOverlayInset = staticCompositionLocalOf { 0.dp }
