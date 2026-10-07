package kurou.kodriver.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * KoDriver アプリ全体の角丸定義。
 * large（16.dp）・extraLarge（28.dp）は Material3 の既定値と同じ値を明示している。
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
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )
