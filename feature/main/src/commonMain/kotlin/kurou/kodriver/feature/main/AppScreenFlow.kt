package kurou.kodriver.feature.main

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.Simulator

/** AppScreen の表示状態を複数の状態 Flow から合成する冷たい Flow。 */
class AppScreenFlow(
    hasAppUpdate: Flow<Boolean>,
    accessLocalNetworkPermissionGranted: Flow<Boolean>,
    keepScreenOn: Flow<Boolean>,
    dynamicColorEnabled: Flow<Boolean>,
    hapticFeedbackEnabled: Flow<Boolean>,
    selectedSimulator: Flow<Simulator>,
) {
    val uiState: Flow<AppScreenUiState> =
        combine(
            combine(hasAppUpdate, accessLocalNetworkPermissionGranted) { hasUpdate, permissionGranted ->
                hasUpdate to permissionGranted
            },
            combine(keepScreenOn, dynamicColorEnabled, hapticFeedbackEnabled) { keepOn, dynamicColor, haptic ->
                Triple(keepOn, dynamicColor, haptic)
            },
            selectedSimulator,
        ) { (hasUpdate, permissionGranted), (keepOn, dynamicColor, haptic), simulator ->
            AppScreenUiState(
                hasAppUpdate = hasUpdate,
                accessLocalNetworkPermissionGranted = permissionGranted,
                keepScreenOn = keepOn,
                dynamicColorEnabled = dynamicColor,
                hapticFeedbackEnabled = haptic,
                selectedSimulator = simulator,
            )
        }
}
