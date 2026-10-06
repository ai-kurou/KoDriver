package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneLabeledTextField
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_DAMAGE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_DAMAGE_PART_DETACHED_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_DAMAGE_TYRE_DETACHED_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.generated.resources.vehicle_damage_description
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.generated.resources.vehicle_damage_overheat_switch_label
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.generated.resources.vehicle_damage_part_detached_switch_label
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.generated.resources.vehicle_damage_text_label
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.generated.resources.vehicle_damage_text_preview
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.generated.resources.vehicle_damage_text_reset_to_default
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.generated.resources.vehicle_damage_text_selected_icon
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.generated.resources.vehicle_damage_text_supporting
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.generated.resources.vehicle_damage_text_unavailable
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.generated.resources.vehicle_damage_tyre_detached_switch_label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LmuWindowsReadoutVehicleDamageDetailPane(modifier: Modifier = Modifier) {
    val viewModel: LmuWindowsReadoutVehicleDamageDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutVehicleDamageDetailPaneContent(
        uiState = uiState,
        onOverheatEnabledChanged = viewModel::onOverheatEnabledChanged,
        onOverheatReadoutTextChanged = viewModel::onOverheatReadoutTextChanged,
        onOverheatReadoutTextPreviewClicked = viewModel::onOverheatReadoutTextPreviewClicked,
        onPartDetachedEnabledChanged = viewModel::onPartDetachedEnabledChanged,
        onPartDetachedReadoutTextChanged = viewModel::onPartDetachedReadoutTextChanged,
        onPartDetachedReadoutTextPreviewClicked = viewModel::onPartDetachedReadoutTextPreviewClicked,
        onTyreDetachedEnabledChanged = viewModel::onTyreDetachedEnabledChanged,
        onTyreDetachedReadoutTextChanged = viewModel::onTyreDetachedReadoutTextChanged,
        onTyreDetachedReadoutTextPreviewClicked = viewModel::onTyreDetachedReadoutTextPreviewClicked,
        modifier = modifier,
    )
}

@Suppress("LongParameterList")
@Composable
internal fun LmuWindowsReadoutVehicleDamageDetailPaneContent(
    uiState: LmuWindowsReadoutVehicleDamageDetailUiState,
    onOverheatEnabledChanged: (Boolean) -> Unit = {},
    onOverheatReadoutTextChanged: (String) -> Unit = {},
    onOverheatReadoutTextPreviewClicked: (String) -> Unit = {},
    onPartDetachedEnabledChanged: (Boolean) -> Unit = {},
    onPartDetachedReadoutTextChanged: (String) -> Unit = {},
    onPartDetachedReadoutTextPreviewClicked: (String) -> Unit = {},
    onTyreDetachedEnabledChanged: (Boolean) -> Unit = {},
    onTyreDetachedReadoutTextChanged: (String) -> Unit = {},
    onTyreDetachedReadoutTextPreviewClicked: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(text = stringResource(Res.string.vehicle_damage_description))
        DetailPaneCard(
            title = stringResource(Res.string.vehicle_damage_overheat_switch_label),
            checked = uiState.overheatEnabled,
            onCheckedChange = onOverheatEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                VehicleDamageReadoutField(
                    text = uiState.overheatReadoutText,
                    defaultText = LMU_WINDOWS_VEHICLE_DAMAGE_OVERHEAT_READOUT_TEXT_DEFAULT,
                    available = uiState.isTextToSpeechAvailable,
                    onTextChanged = onOverheatReadoutTextChanged,
                    onPreviewClick = onOverheatReadoutTextPreviewClicked,
                )
            },
        )
        DetailPaneCard(
            title = stringResource(Res.string.vehicle_damage_part_detached_switch_label),
            checked = uiState.partDetachedEnabled,
            onCheckedChange = onPartDetachedEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                VehicleDamageReadoutField(
                    text = uiState.partDetachedReadoutText,
                    defaultText = LMU_WINDOWS_VEHICLE_DAMAGE_PART_DETACHED_READOUT_TEXT_DEFAULT,
                    available = uiState.isTextToSpeechAvailable,
                    onTextChanged = onPartDetachedReadoutTextChanged,
                    onPreviewClick = onPartDetachedReadoutTextPreviewClicked,
                )
            },
        )
        DetailPaneCard(
            title = stringResource(Res.string.vehicle_damage_tyre_detached_switch_label),
            checked = uiState.tyreDetachedEnabled,
            onCheckedChange = onTyreDetachedEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                VehicleDamageReadoutField(
                    text = uiState.tyreDetachedReadoutText,
                    defaultText = LMU_WINDOWS_VEHICLE_DAMAGE_TYRE_DETACHED_READOUT_TEXT_DEFAULT,
                    available = uiState.isTextToSpeechAvailable,
                    onTextChanged = onTyreDetachedReadoutTextChanged,
                    onPreviewClick = onTyreDetachedReadoutTextPreviewClicked,
                )
            },
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun VehicleDamageReadoutField(
    text: String,
    defaultText: String,
    available: Boolean,
    onTextChanged: (String) -> Unit,
    onPreviewClick: (String) -> Unit,
) {
    var currentText by remember { mutableStateOf(text) }
    // 保存が非同期のため、入力中の値を保存済みの古い値で巻き戻さない。
    var pendingText by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(text, pendingText) {
        if (pendingText == null) {
            currentText = text
        } else if (pendingText == text) {
            pendingText = null
        }
    }
    val changeText: (String) -> Unit = {
        currentText = it
        pendingText = it.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH)
        onTextChanged(it)
    }
    Column {
        val label = stringResource(Res.string.vehicle_damage_text_label)
        DetailPaneLabeledTextField(
            label = label,
            value = currentText,
            defaultValue = defaultText,
            onResetToDefault = { changeText(defaultText) },
            resetContentDescription = stringResource(Res.string.vehicle_damage_text_reset_to_default),
            maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
            onValueChangeFinished = changeText,
            onPreviewClick = onPreviewClick,
            enabled = available,
            selected = currentText.isNotBlank(),
            supportingText =
                when {
                    !available -> stringResource(Res.string.vehicle_damage_text_unavailable)
                    currentText.isNotBlank() -> null
                    else -> stringResource(Res.string.vehicle_damage_text_supporting)
                },
            previewContentDescription = stringResource(Res.string.vehicle_damage_text_preview),
            selectedContentDescription = stringResource(Res.string.vehicle_damage_text_selected_icon),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutVehicleDamageDetailPanePreview() {
    KoDriverTheme {
        LmuWindowsReadoutVehicleDamageDetailPaneContent(uiState = LmuWindowsReadoutVehicleDamageDetailUiState())
    }
}
