package kurou.kodriver.presentation

import androidx.compose.ui.test.SemanticsNodeInteraction
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage

private val defaultOptions = RoborazziOptions(
    compareOptions = RoborazziOptions.CompareOptions(
        changeThreshold = 0.01f,
    ),
)

internal fun SemanticsNodeInteraction.captureRoboImage() =
    captureRoboImage(roborazziOptions = defaultOptions)
