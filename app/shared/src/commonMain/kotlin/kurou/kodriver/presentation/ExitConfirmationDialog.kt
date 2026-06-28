package kurou.kodriver.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kodriver.app.shared.generated.resources.Res
import kodriver.app.shared.generated.resources.exit_confirmation_body
import kodriver.app.shared.generated.resources.exit_confirmation_cancel
import kodriver.app.shared.generated.resources.exit_confirmation_do_not_show_again
import kodriver.app.shared.generated.resources.exit_confirmation_exit
import kodriver.app.shared.generated.resources.exit_confirmation_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ExitConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: (doNotShowAgain: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var doNotShowAgain by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.exit_confirmation_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(Res.string.exit_confirmation_body),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { doNotShowAgain = !doNotShowAgain },
                ) {
                    Checkbox(
                        checked = doNotShowAgain,
                        onCheckedChange = { doNotShowAgain = it },
                    )
                    Text(stringResource(Res.string.exit_confirmation_do_not_show_again))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(doNotShowAgain) }) {
                Text(stringResource(Res.string.exit_confirmation_exit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.exit_confirmation_cancel))
            }
        },
        modifier = modifier,
    )
}
