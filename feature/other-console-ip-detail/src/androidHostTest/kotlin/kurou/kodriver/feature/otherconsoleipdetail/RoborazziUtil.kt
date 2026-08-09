package kurou.kodriver.feature.otherconsoleipdetail

import com.github.takahirom.roborazzi.RoborazziOptions

internal val defaultRoborazziOptions =
    RoborazziOptions(
        compareOptions =
            RoborazziOptions.CompareOptions(
                changeThreshold = 0.001f,
            ),
    )
