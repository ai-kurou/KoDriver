package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.KoDriverHotkeyCard
import kurou.kodriver.core.designsystem.KoDriverHotkeyCardTexts
import kurou.kodriver.core.designsystem.KoDriverHotkeyRegistration
import kurou.kodriver.core.designsystem.hotkeyDisplayName
import kurou.kodriver.core.designsystem.simulatorDisplayName
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.Res
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_clear
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_guide_link
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_hint
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_none
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_other_set
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_other_set_named
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_registration_conflict
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_registration_failed
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_registration_registered
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_registration_registering
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_registration_retry
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ManualReadoutHotkeyCard(
    state: ManualReadoutSetDetailUiState,
    onAssign: (Int) -> Unit,
    onClear: () -> Unit,
    onRetry: () -> Unit,
    onSelectModifiers: (ManualReadoutHotkeyModifiers) -> Unit,
    onOpenGuide: () -> Unit,
    showGuideLink: Boolean = true,
) {
    KoDriverHotkeyCard(
        texts =
            KoDriverHotkeyCardTexts(
                title = stringResource(Res.string.manual_readout_hotkey_title),
                clear = stringResource(Res.string.manual_readout_hotkey_clear),
                retry = stringResource(Res.string.manual_readout_hotkey_registration_retry),
                hint = stringResource(Res.string.manual_readout_hotkey_hint),
                guideLink = stringResource(Res.string.manual_readout_hotkey_guide_link),
            ),
        modifierOptions = ManualReadoutHotkeyModifiers.entries,
        selectedModifier = state.hotkeyModifiers,
        modifierLabel = { it.displayName() },
        onSelectModifier = onSelectModifiers,
        keyText =
            state.hotkeyVirtualKeyCode?.let { hotkeyDisplayName(it) }
                ?: stringResource(Res.string.manual_readout_hotkey_none),
        onAssign = onAssign,
        clearEnabled = state.hotkeyVirtualKeyCode != null,
        onClear = onClear,
        errorText = manualReadoutHotkeyConflictText(state.hotkeyConflict),
        registration =
            if (state.hotkeyVirtualKeyCode == null) {
                null
            } else {
                manualReadoutHotkeyRegistration(state.hotkeyRegistrationState)
            },
        onRetry = onRetry,
        onOpenGuide = onOpenGuide,
        testTagPrefix = "manual_readout_hotkey",
        showGuideLink = showGuideLink,
    )
}

@Composable
private fun manualReadoutHotkeyConflictText(conflict: ManualReadoutHotkeyConflict?): String? =
    when (conflict) {
        is ManualReadoutHotkeyConflict.OtherSet -> {
            if (conflict.name.isBlank()) {
                stringResource(
                    Res.string.manual_readout_hotkey_other_set,
                    simulatorDisplayName(conflict.simulatorId),
                    conflict.setNumber,
                )
            } else {
                stringResource(
                    Res.string.manual_readout_hotkey_other_set_named,
                    simulatorDisplayName(conflict.simulatorId),
                    conflict.name,
                )
            }
        }

        null -> {
            null
        }
    }

@Composable
private fun manualReadoutHotkeyRegistration(registration: HotkeyRegistrationState): KoDriverHotkeyRegistration? =
    when (registration) {
        HotkeyRegistrationState.Unsupported -> {
            null
        }

        HotkeyRegistrationState.Registering -> {
            KoDriverHotkeyRegistration(stringResource(Res.string.manual_readout_hotkey_registration_registering))
        }

        HotkeyRegistrationState.Registered -> {
            KoDriverHotkeyRegistration(stringResource(Res.string.manual_readout_hotkey_registration_registered))
        }

        HotkeyRegistrationState.Conflict -> {
            KoDriverHotkeyRegistration(
                text = stringResource(Res.string.manual_readout_hotkey_registration_conflict),
                canRetry = true,
            )
        }

        is HotkeyRegistrationState.Failed -> {
            KoDriverHotkeyRegistration(
                text =
                    stringResource(
                        Res.string.manual_readout_hotkey_registration_failed,
                        registration.errorCode,
                    ),
                canRetry = true,
            )
        }
    }
