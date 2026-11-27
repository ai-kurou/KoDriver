package kurou.kodriver.core.designsystem

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * [KoDriverHotkeyCard] の固定文言。呼び出し側で文字列リソースを解決して渡す。
 */
public data class KoDriverHotkeyCardTexts(
    val title: String,
    val clear: String,
    val retry: String,
    val hint: String,
    val guideLink: String,
)

/**
 * [KoDriverHotkeyCard] に表示する登録状態。[canRetry] が true のときは再試行ボタンを表示する。
 */
public data class KoDriverHotkeyRegistration(
    val text: String,
    val canRetry: Boolean = false,
)

/**
 * 修飾キーの選択とキー入力でホットキーを割り当てるカード。
 *
 * [keyText] は入力欄の表示文字列。[errorText] と [registration] は null のとき非表示にする。
 * テストタグは [testTagPrefix] を接頭辞にして付ける。修飾キーの選択肢のタグには各値の toString() を使う。
 */
@Composable
public fun <T> KoDriverHotkeyCard(
    texts: KoDriverHotkeyCardTexts,
    modifierOptions: List<T>,
    selectedModifier: T,
    modifierLabel: (T) -> String,
    onSelectModifier: (T) -> Unit,
    keyText: String,
    onAssign: (Int) -> Unit,
    clearEnabled: Boolean,
    onClear: () -> Unit,
    errorText: String?,
    registration: KoDriverHotkeyRegistration?,
    onRetry: () -> Unit,
    onOpenGuide: () -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier,
    showGuideLink: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current
    // 非表示への切り替えで入力が消えても、退場アニメーション中は最後の表示内容を保つ。
    var lastErrorText by remember { mutableStateOf(errorText) }
    var lastRegistration by remember { mutableStateOf(registration) }
    if (errorText != null) {
        lastErrorText = errorText
    }
    if (registration != null) {
        lastRegistration = registration
    }
    DetailPaneCard(
        title = texts.title,
        modifier = modifier.testTag("${testTagPrefix}_card"),
        bottomContent = {
            Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
                Row(horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
                    HotkeyModifiersDropdown(
                        options = modifierOptions,
                        selected = selectedModifier,
                        label = modifierLabel,
                        onSelect = onSelectModifier,
                        testTag = "${testTagPrefix}_modifiers",
                    )
                    HotkeyKeyField(
                        keyText = keyText,
                        onAssign = onAssign,
                        modifier = Modifier.weight(1f).testTag("${testTagPrefix}_key"),
                    )
                }
                KoDriverCollapsibleVisibility(visible = errorText != null) {
                    Text(lastErrorText.orEmpty(), color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onClear()
                }, enabled = clearEnabled) {
                    Text(texts.clear)
                }
                KoDriverCollapsibleVisibility(visible = registration != null) {
                    lastRegistration?.let { shownRegistration ->
                        HotkeyRegistration(shownRegistration, texts.retry) {
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            onRetry()
                        }
                    }
                }
                Text(texts.hint)
                KoDriverCollapsibleVisibility(visible = showGuideLink, expandFrom = Alignment.Bottom) {
                    HotkeyGuideLink(texts.guideLink, onOpenGuide)
                }
            }
        },
    )
}

@Composable
private fun HotkeyKeyField(
    keyText: String,
    onAssign: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    OutlinedTextField(
        value = keyText,
        onValueChange = {},
        readOnly = true,
        modifier =
            modifier
                .onPreviewKeyEvent { event ->
                    val code = hotkeyVirtualKeyCode(event.key)
                    if (event.type == KeyEventType.KeyDown && code != null) {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onAssign(code)
                        true
                    } else {
                        false
                    }
                },
    )
}

@Composable
private fun HotkeyGuideLink(
    text: String,
    onOpenGuide: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier.clickable {
                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                onOpenGuide()
            },
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.size(KoDriverSpacing.extraSmall))
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(12.dp),
        )
    }
}

@Composable
private fun HotkeyRegistration(
    registration: KoDriverHotkeyRegistration,
    retryText: String,
    onRetry: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
        Crossfade(targetState = registration.text, label = "hotkeyRegistration") { animatedText ->
            Text(animatedText)
        }
        KoDriverCollapsibleVisibility(visible = registration.canRetry) {
            TextButton(onClick = onRetry) {
                Text(retryText)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> HotkeyModifiersDropdown(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    testTag: String,
) {
    val haptic = LocalHapticFeedback.current
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = {
        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        expanded = it
    }) {
        OutlinedTextField(
            value = label(selected),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier =
                Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .testTag(testTag),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { value ->
                DropdownMenuItem(
                    text = { Text(label(value)) },
                    modifier = Modifier.testTag("${testTag}_$value"),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onSelect(value)
                        expanded = false
                    },
                )
            }
        }
    }
}
