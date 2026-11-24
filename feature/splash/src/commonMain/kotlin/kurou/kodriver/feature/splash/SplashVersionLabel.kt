package kurou.kodriver.feature.splash

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import kurou.kodriver.feature.splash.generated.resources.Res
import kurou.kodriver.feature.splash.generated.resources.splash_version
import org.jetbrains.compose.resources.stringResource

/**
 * スプラッシュに表示するアプリのバージョン（例: `v5.3.0`）。[appVersion] は `v` を付けずに渡す。
 * 色・文字スタイルは背景に合わせて呼び出し側が [color]・[style] で決める。
 * デスクトップと Android のスプラッシュで共通に使う。
 */
@Composable
public fun SplashVersionLabel(
    appVersion: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    style: TextStyle = MaterialTheme.typography.labelMedium,
) {
    Text(
        text = stringResource(Res.string.splash_version, appVersion),
        modifier = modifier,
        color = color,
        style = style,
    )
}
