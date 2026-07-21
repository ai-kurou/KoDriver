package kurou.kodriver.feature.debugstatedetail

import com.github.takahirom.roborazzi.RoborazziOptions

internal val defaultRoborazziOptions = RoborazziOptions(
    compareOptions = RoborazziOptions.CompareOptions(
        changeThreshold = 0.02f,
    ),
)
