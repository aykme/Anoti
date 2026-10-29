package com.alekseivinogradov.anoti.infodialog.kmp.impl.presentation.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Cinnabar500
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Grey700
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.SilverTransparent
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.White
import com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.compose.AnotiTheme
import com.alekseivinogradov.anoti.infodialog.kmp.api.presentation.compose.INFO_DIALOG_LARGE_FONT_PREVIEW_HEIGHT_DP
import com.alekseivinogradov.anoti.infodialog.kmp.api.presentation.compose.INFO_DIALOG_LINKS_GAP_DP
import com.alekseivinogradov.anoti.infodialog.kmp.api.presentation.compose.INFO_DIALOG_PREVIEW_HEIGHT_DP
import com.alekseivinogradov.anoti.infodialog.kmp.api.presentation.compose.INFO_DIALOG_PREVIEW_WIDTH_DP
import com.alekseivinogradov.anoti.infodialog.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.infodialog.kmp.generated.resources.info_dialog_close
import com.alekseivinogradov.anoti.infodialog.kmp.generated.resources.info_dialog_privacy_policy
import com.alekseivinogradov.anoti.infodialog.kmp.generated.resources.info_dialog_support
import com.alekseivinogradov.anoti.infodialog.kmp.generated.resources.info_dialog_title
import com.alekseivinogradov.anoti.infodialog.kmp.generated.resources.open_in_browser_description
import org.jetbrains.compose.resources.stringResource

/**
 * Shows where the privacy policy and the support page live. The host decides when it shows and
 * acts on each choice; this composable only renders them.
 *
 * @param onPrivacyPolicyClick the privacy policy link was tapped.
 * @param onSupportClick the support link was tapped.
 * @param onClose Close, Back or a tap outside asked to close the dialog.
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
fun InfoDialog(
    onPrivacyPolicyClick: () -> Unit,
    onSupportClick: () -> Unit,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        containerColor = Grey700,
        modifier = Modifier.testTag("info_dialog"),
        title = {
            Text(
                text = stringResource(Res.string.info_dialog_title),
                color = White,
                modifier = Modifier.testTag("info_dialog_title")
            )
        },
        text = {
            // A window too low for both links scrolls them and keeps the title and Close in view.
            Column(Modifier.verticalScroll(rememberScrollState())) {
                InfoDialogLink(
                    text = stringResource(Res.string.info_dialog_privacy_policy),
                    testTag = "info_dialog_privacy_policy",
                    onClick = onPrivacyPolicyClick
                )
                Spacer(Modifier.height(INFO_DIALOG_LINKS_GAP_DP.dp))
                InfoDialogLink(
                    text = stringResource(Res.string.info_dialog_support),
                    testTag = "info_dialog_support",
                    onClick = onSupportClick
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onClose, modifier = Modifier.testTag("info_dialog_close")) {
                Text(text = stringResource(Res.string.info_dialog_close), color = Cinnabar500)
            }
        }
    )
}

@Suppress("FunctionNaming")
@Composable
private fun InfoDialogLink(text: String, testTag: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = SilverTransparent,
        // Tells a link apart without relying on its color.
        textDecoration = TextDecoration.Underline,
        // The click comes before the minimum size, so the whole row of at least 48 dp answers.
        modifier = Modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clickable(
                onClickLabel = stringResource(Res.string.open_in_browser_description),
                onClick = onClick
            )
            .minimumInteractiveComponentSize()
    )
}

@Suppress("FunctionNaming", "UnusedPrivateMember")
@Preview(widthDp = INFO_DIALOG_PREVIEW_WIDTH_DP, heightDp = INFO_DIALOG_PREVIEW_HEIGHT_DP)
@Composable
private fun InfoDialogPreview() {
    AnotiTheme {
        InfoDialog(onPrivacyPolicyClick = {}, onSupportClick = {}, onClose = {})
    }
}

@Suppress("FunctionNaming", "UnusedPrivateMember")
@Preview(
    widthDp = INFO_DIALOG_PREVIEW_WIDTH_DP,
    heightDp = INFO_DIALOG_LARGE_FONT_PREVIEW_HEIGHT_DP,
    fontScale = 2f
)
@Composable
private fun InfoDialogLargeFontPreview() {
    AnotiTheme {
        InfoDialog(onPrivacyPolicyClick = {}, onSupportClick = {}, onClose = {})
    }
}
