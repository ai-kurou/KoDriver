package kurou.kodriver.presentation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions

internal val defaultRoborazziOptions = RoborazziOptions(
    compareOptions = RoborazziOptions.CompareOptions(
        changeThreshold = 0.02f,
    ),
)

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
internal val twoPaneDirective = PaneScaffoldDirective(
    maxHorizontalPartitions = 2,
    horizontalPartitionSpacerSize = 16.dp,
    maxVerticalPartitions = 1,
    verticalPartitionSpacerSize = 0.dp,
    defaultPanePreferredWidth = 360.dp,
    excludedBounds = emptyList(),
)
