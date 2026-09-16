package kurou.kodriver.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * KoDriver アプリ全体の角丸定義。
 *
 * [KoDriverTheme] を通じて全画面へ配布されるため、角丸の調整はこのファイルの変更だけで完結する。
 *
 * feature モジュール側では `RoundedCornerShape` を直接指定せず、`MaterialTheme.shapes.*` のスタイルだけを参照すること。
 */
val KoDriverShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(6.dp),
        medium = RoundedCornerShape(10.dp),
    )
