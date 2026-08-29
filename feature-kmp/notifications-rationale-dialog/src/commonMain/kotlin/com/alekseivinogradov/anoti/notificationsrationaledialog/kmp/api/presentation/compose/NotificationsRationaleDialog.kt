package com.alekseivinogradov.anoti.notificationsrationaledialog.kmp.api.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.AnotiTheme
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Cinnabar500
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Grey700
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.SilverTransparent
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.White
import com.alekseivinogradov.anoti.notificationsrationaledialog.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.notificationsrationaledialog.kmp.generated.resources.dialog_alert_negative_button
import com.alekseivinogradov.anoti.notificationsrationaledialog.kmp.generated.resources.dialog_alert_notifications_rationale_message
import com.alekseivinogradov.anoti.notificationsrationaledialog.kmp.generated.resources.dialog_alert_positive_button
import com.alekseivinogradov.anoti.notificationsrationaledialog.kmp.generated.resources.dialog_alert_title
import org.jetbrains.compose.resources.stringResource

/**
 * Asks the user to grant (or open settings for) the notifications permission, after the OS has
 * denied an earlier direct request. The host is responsible for deciding when to show it and for
 * acting on the outcome — this composable only renders the prompt.
 *
 * @param icon the dialog's icon; the host supplies it so this module doesn't need its own copy
 * of the app icon.
 * @param onDismiss called when the user declines or dismisses the dialog.
 * @param onApprove called when the user accepts; the host is expected to then request the
 * permission again or open the app's notification settings, depending on OS version.
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
fun NotificationsRationaleDialog(
    icon: @Composable () -> Unit,
    onDismiss: () -> Unit,
    onApprove: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Grey700,
        icon = icon,
        title = { Text(text = stringResource(Res.string.dialog_alert_title), color = White) },
        text = {
            Text(
                text = stringResource(Res.string.dialog_alert_notifications_rationale_message),
                color = SilverTransparent
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(Res.string.dialog_alert_negative_button),
                    color = SilverTransparent
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onApprove) {
                Text(
                    text = stringResource(Res.string.dialog_alert_positive_button),
                    color = Cinnabar500
                )
            }
        }
    )
}

@Suppress("FunctionNaming", "UnusedPrivateMember")
@Preview(widthDp = PREVIEW_WIDTH_DP, heightDp = PREVIEW_HEIGHT_DP)
@Composable
private fun NotificationsRationaleDialogPreview() {
    AnotiTheme {
        NotificationsRationaleDialog(
            icon = {
                Box(
                    modifier = Modifier
                        .size(PREVIEW_ICON_SIZE_DP)
                        .background(Cinnabar500, CircleShape)
                )
            },
            onDismiss = {},
            onApprove = {}
        )
    }
}
