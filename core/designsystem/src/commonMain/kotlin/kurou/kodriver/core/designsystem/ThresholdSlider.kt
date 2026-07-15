package kurou.kodriver.core.designsystem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ThresholdSlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    labelFormatter: (Float) -> String,
    onValueChangeFinished: (Float) -> Unit,
    modifier: Modifier = Modifier,
    steps: Int = (((valueRange.endInclusive - valueRange.start) / 0.1f).roundToInt() - 1).coerceAtLeast(0),
    defaultValue: Float? = null,
    onResetToDefault: (() -> Unit)? = null,
    resetContentDescription: String? = null,
) {
    var sliderValue by remember(value) { mutableStateOf(value) }
    val isDifferentFromDefault = defaultValue != null && abs(sliderValue - defaultValue) > 0.001f
    val resetButtonAlpha by animateFloatAsState(targetValue = if (isDifferentFromDefault) 1f else 0f)

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = labelFormatter(sliderValue),
                modifier = Modifier.weight(1f),
            )
            if (onResetToDefault != null) {
                IconButton(
                    onClick = onResetToDefault,
                    enabled = isDifferentFromDefault,
                    modifier = Modifier.size(32.dp).graphicsLayer { alpha = resetButtonAlpha },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RestartAlt,
                        contentDescription = resetContentDescription,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = { onValueChangeFinished(sliderValue) },
        )
    }
}
