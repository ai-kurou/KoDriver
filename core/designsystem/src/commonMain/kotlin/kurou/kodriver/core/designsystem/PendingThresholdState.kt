package kurou.kodriver.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

internal const val PENDING_THRESHOLD_TIMEOUT_MILLIS_DEFAULT = 3000L

/** 操作中と保存待ちの閾値を保持し、途中の保存通知による巻き戻りを防ぐ。 */
@Stable
class PendingThresholdState internal constructor(
    savedValue: Int,
) {
    var currentValue by mutableIntStateOf(savedValue)
        private set

    internal var pendingValues by mutableStateOf<List<Int>>(emptyList())
        private set

    private var lastSaved by mutableIntStateOf(savedValue)
    private var isEditing by mutableStateOf(false)

    /** 操作中の表示値を更新する。保存は操作完了時に別途行う。 */
    fun change(value: Int) {
        currentValue = value
        isEditing = true
    }

    /** 操作完了またはリセットの値を表示し、保存通知を待つ。 */
    fun finishChange(value: Int) {
        currentValue = value
        isEditing = false
        if (pendingValues.isNotEmpty() || value != lastSaved) {
            pendingValues = pendingValues + value
        }
    }

    internal fun updateSavedValue(savedValue: Int) {
        if (savedValue == lastSaved) return
        lastSaved = savedValue
        if (pendingValues.isNotEmpty()) {
            val firstMatch = pendingValues.indexOf(savedValue)
            if (firstMatch >= 0) {
                pendingValues = pendingValues.drop(firstMatch + 1)
            }
        }
        if (pendingValues.isEmpty() && !isEditing) {
            currentValue = savedValue
        }
    }

    /** 保存通知が省略されても、表示値を保持したまま保存待ちを解除する。 */
    internal fun expirePending() {
        pendingValues = emptyList()
    }
}

/** 保存値を同期し、保存待ちの変更から期限が過ぎると外部更新への追従を再開する。 */
@Composable
fun rememberPendingThreshold(
    savedValue: Int,
    pendingTimeoutMillis: Long = PENDING_THRESHOLD_TIMEOUT_MILLIS_DEFAULT,
): PendingThresholdState {
    val state = remember { PendingThresholdState(savedValue) }
    LaunchedEffect(savedValue) {
        state.updateSavedValue(savedValue)
    }
    val pendingValues = state.pendingValues
    LaunchedEffect(pendingValues, pendingTimeoutMillis) {
        if (pendingValues.isNotEmpty()) {
            delay(pendingTimeoutMillis)
            state.expirePending()
        }
    }
    return state
}
