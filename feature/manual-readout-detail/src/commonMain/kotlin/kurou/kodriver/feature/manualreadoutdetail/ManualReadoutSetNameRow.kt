package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import kurou.kodriver.domain.model.takeManualReadoutSetName
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.Res
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_set_name
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_set_name_confirm
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_set_name_edit
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_set_title
import org.jetbrains.compose.resources.stringResource

/** セット名を画面内で編集し、確定時だけ保存する。 */
@Composable
internal fun ManualReadoutSetNameRow(
    name: String,
    number: Int,
    onNameChanged: (String) -> Unit,
) {
    var editing by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf(name) }
    var hadFocus by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val defaultName = stringResource(Res.string.manual_readout_set_title, number)
    val startEditing = {
        input = name
        hadFocus = false
        editing = true
    }
    val finishEditing = {
        if (editing) {
            editing = false
            onNameChanged(input)
        }
    }
    if (editing) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
        OutlinedTextField(
            value = input,
            onValueChange = { input = it.trimStart().takeManualReadoutSetName() },
            singleLine = true,
            label = { Text(stringResource(Res.string.manual_readout_set_name)) },
            placeholder = { Text(defaultName) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { finishEditing() }),
            trailingIcon = {
                IconButton(
                    onClick = finishEditing,
                    modifier = Modifier.focusProperties { canFocus = false },
                ) {
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = stringResource(Res.string.manual_readout_set_name_confirm),
                    )
                }
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag("manual_readout_set_name_input")
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        if (it.isFocused) {
                            hadFocus = true
                        } else if (hadFocus) {
                            finishEditing()
                        }
                    },
        )
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = name.ifBlank { defaultName },
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f).testTag("manual_readout_set_name").clickable(onClick = startEditing),
            )
            IconButton(onClick = startEditing) {
                Icon(
                    Icons.Outlined.Edit,
                    contentDescription = stringResource(Res.string.manual_readout_set_name_edit),
                )
            }
        }
    }
}
