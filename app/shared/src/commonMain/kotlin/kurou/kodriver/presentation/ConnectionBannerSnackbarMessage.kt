package kurou.kodriver.presentation

import kurou.kodriver.app.shared.generated.resources.Res
import kurou.kodriver.app.shared.generated.resources.ace_connected
import kurou.kodriver.app.shared.generated.resources.ace_disconnected
import kurou.kodriver.app.shared.generated.resources.gt7_connected
import kurou.kodriver.app.shared.generated.resources.gt7_disconnected
import kurou.kodriver.app.shared.generated.resources.lmu_connected
import kurou.kodriver.app.shared.generated.resources.lmu_disconnected
import org.jetbrains.compose.resources.StringResource

internal fun connectionBannerSnackbarConnectedMessageRes(
    isGt7: Boolean,
    isAceWindows: Boolean,
): StringResource =
    when {
        isGt7 -> Res.string.gt7_connected
        isAceWindows -> Res.string.ace_connected
        else -> Res.string.lmu_connected
    }

internal fun connectionBannerSnackbarDisconnectedMessageRes(
    isGt7: Boolean,
    isAceWindows: Boolean,
): StringResource =
    when {
        isGt7 -> Res.string.gt7_disconnected
        isAceWindows -> Res.string.ace_disconnected
        else -> Res.string.lmu_disconnected
    }
