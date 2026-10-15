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
    selection: TextRange?,
    insertion: String,
    maxLength: Int,
): TextFieldValue? {
    // 選択範囲が未操作（null）の場合は末尾へ追加する。
    val range = selection ?: TextRange(text.length)
    val start = range.min.coerceIn(0, text.length)
    val end = range.max.coerceIn(0, text.length)
    val prefix = (text.take(start) + insertion).replace("\n", "")
    val result = prefix + text.substring(end).replace("\n", "")
    return if (result.length > maxLength) {
        null
    } else {
        TextFieldValue(result, TextRange(prefix.length))
    }
}

/**
 * 改行を除去して [maxLength] で切り詰め、選択範囲を除去後のオフセットへ写像する。
 * IMEの変換中範囲は、テキストが加工されなかった場合のみ引き継ぐ。
 */
internal fun normalizeInput(
    input: TextFieldValue,
    maxLength: Int,
): TextFieldValue {
    val text = input.text.replace("\n", "").take(maxLength)

    fun mapOffset(offset: Int): Int {
        val removed = input.text.take(offset).count { it == '\n' }
        return (offset - removed).coerceIn(0, text.length)
    }
    return TextFieldValue(
        text,
        TextRange(mapOffset(input.selection.start), mapOffset(input.selection.end)),
        input.composition.takeIf { text == input.text },
    )
}
