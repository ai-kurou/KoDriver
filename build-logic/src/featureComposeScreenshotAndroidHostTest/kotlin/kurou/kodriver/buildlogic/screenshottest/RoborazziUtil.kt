package kurou.kodriver.buildlogic.screenshottest

import com.github.takahirom.roborazzi.RoborazziOptions

val defaultRoborazziOptions =
    RoborazziOptions(
        compareOptions =
            RoborazziOptions.CompareOptions(
                changeThreshold = 0.001f,
            ),
    )
