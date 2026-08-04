package kurou.kodriver.buildlogic.screenshottest

import androidx.compose.ui.test.SemanticsNodeInteraction
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage

val defaultRoborazziOptions =
    RoborazziOptions(
        compareOptions =
            RoborazziOptions.CompareOptions(
                changeThreshold = 0.001f,
            ),
    )

fun SemanticsNodeInteraction.captureRoboImage() = captureRoboImage(roborazziOptions = defaultRoborazziOptions)
