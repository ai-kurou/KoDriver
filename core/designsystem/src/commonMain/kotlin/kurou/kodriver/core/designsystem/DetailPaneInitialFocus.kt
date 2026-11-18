package kurou.kodriver.core.designsystem

import androidx.compose.foundation.focusGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalInputModeManager

/**
 * 詳細ペインの本文先頭の操作部品へ初期フォーカスを置くための Modifier。
 *
 * キーボード操作のときだけ、フォーカスグループへフォーカスを要求する。グループ内の最初のフォーカス可能な子に当たる。
 * 判定はペインに入った時点の一度きりで、タッチ操作ではフォーカスを要求しない。フォーカス可能な子がなければ何も起きない。
 *
 * 別モジュールの詳細ペインから利用できるよう公開している。
 */
@Composable
public fun Modifier.detailPaneInitialFocus(): Modifier {
    val focusRequester = remember { FocusRequester() }
    val inputModeManager = LocalInputModeManager.current
    // 入力モードは詳細ペインに入った時点で一度だけ判定する。表示中にタッチからキーボードへ切り替わっても、フォーカスを奪わない
    val requestsFocus = remember { shouldRequestInitialFocus(inputModeManager.inputMode) }
    if (requestsFocus) {
        LaunchedEffect(Unit) {
            withFrameNanos { }
            runCatching { focusRequester.requestFocus() }
        }
    }
    return this.focusRequester(focusRequester).focusGroup()
}

/** [inputMode] がキーボード操作のときだけ初期フォーカスを要求する。 */
internal fun shouldRequestInitialFocus(inputMode: InputMode): Boolean = inputMode == InputMode.Keyboard
