package kurou.kodriver.core.designsystem

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/** 数値を大きく表示する。[text] が null の場合は未取得表示にする。 */
@Composable
fun DebugStateHeadlineValueContent(text: String?) {
    if (text == null) {
        DebugStateUnavailableContent()
    } else {
        Text(
            text = text,
            style = koDriverNumericTextStyle(MaterialTheme.typography.headlineMedium),
        )
    }
}

/** 文字列をそのまま表示する。[text] が null または空の場合は未取得表示にする。 */
@Composable
fun DebugStateTextValueContent(text: String?) {
    if (text.isNullOrEmpty()) {
        DebugStateUnavailableContent()
    } else {
        Text(text = text)
    }
}

/**
 * [state] の表示内容が変わったときだけフェードで切り替える。
 *
 * 表示内容が変わらないテレメトリの更新ではフェードし直さないよう、[contentKey] で表示内容だけをキーにする。
 */
@Composable
fun <S, K> DebugStateFadeContent(
    state: S,
    contentKey: (S) -> K,
    content: @Composable (S) -> Unit,
) {
    AnimatedContent(
        targetState = state,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentKey = contentKey,
    ) { target ->
        content(target)
    }
}
