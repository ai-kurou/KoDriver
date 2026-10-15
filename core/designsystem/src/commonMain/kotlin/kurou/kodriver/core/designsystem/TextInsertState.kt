package kurou.kodriver.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/** 入力欄のカーソル位置への挿入要求を保持する。入力欄ごとに用意する。 */
@Stable
class TextInsertState {
    internal var request by mutableStateOf<InsertRequest?>(null)
        private set

    /** 選択範囲がある場合は、その範囲を [text] で置換する。 */
    fun insert(text: String) {
        request = InsertRequest(text)
    }

    internal fun consume(request: InsertRequest) {
        if (this.request === request) {
            this.request = null
        }
    }

    internal class InsertRequest(
        val text: String,
    )
}

/** 再コンポーズをまたいで挿入要求の状態を保持する。 */
@Composable
fun rememberTextInsertState(): TextInsertState = remember { TextInsertState() }

/** 選択範囲を置換し、改行除去後に上限を超える場合は挿入しない。 */
internal fun insertAtSelection(
    text: String,
    selection: TextRange,
    insertion: String,
    maxLength: Int,
): TextFieldValue? {
    val start = selection.min.coerceIn(0, text.length)
    val end = selection.max.coerceIn(0, text.length)
    val prefix = (text.take(start) + insertion).replace("\n", "")
    val result = prefix + text.substring(end).replace("\n", "")
    return if (result.length > maxLength) {
        null
    } else {
        TextFieldValue(result, TextRange(prefix.length))
    }
}
