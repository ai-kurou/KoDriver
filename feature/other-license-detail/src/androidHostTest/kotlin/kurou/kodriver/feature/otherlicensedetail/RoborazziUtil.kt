package kurou.kodriver.feature.otherlicensedetail

import com.github.takahirom.roborazzi.RoborazziOptions

internal val defaultRoborazziOptions = RoborazziOptions(
    compareOptions = RoborazziOptions.CompareOptions(
        changeThreshold = 0.02f,
    ),
)
