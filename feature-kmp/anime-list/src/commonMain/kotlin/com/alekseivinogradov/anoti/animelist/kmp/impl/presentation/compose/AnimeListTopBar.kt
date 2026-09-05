package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.SEARCH_TEXT_MAX_LENGTH
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.SEARCH_ICON_OFFSET_X_DP
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.SEARCH_ICON_OFFSET_Y_DP
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.TOP_BAR_CONTROL_SIZE_DP
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.TOP_BAR_PREVIEW_HEIGHT_DP
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.TOP_BAR_PREVIEW_WIDTH_DP
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.SearchUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.SectionHatUi
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.ic_search_32
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.ic_search_34
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.ic_search_cancel_32
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.on_air
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.search_hint
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.search_off_description
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.search_on_description
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.soon
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.AnotiTheme
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.BlackTransparent
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Cinnabar500
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.LARGE_ACCENT_TEXT_SP
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.SUBTITLE1_SP
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.White
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.WhiteTransparent
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.systemBarsTopPadding
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.amiko
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res as CelebrityRes

/**
 * The anime list screen's top bar: section tabs (ongoing/announced) plus a search toggle, or
 * the search field itself, depending on [search].
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming", "LongParameterList")
@Composable
fun AnimeListTopBar(
    selectedSection: SectionHatUi,
    search: SearchUi,
    onOngoingClick: () -> Unit,
    onAnnouncedClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCancelSearchClick: () -> Unit,
    onSearchTextChange: (String) -> Unit
) {
    // Hoisted above the `search == SHOWN` check so typed text survives closing and reopening
    // the search bar; saveable so it also survives process death, matching the restored search
    // this text drives (see NavAnimeListScreenComponent.applyRestoredMainState).
    var searchText by rememberSaveable { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(search) {
        if (search == SearchUi.HIDDEN) {
            keyboardController?.hide()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = systemBarsTopPadding())
    ) {
        if (search == SearchUi.HIDDEN) {
            TabsRow(
                selectedSection = selectedSection,
                onOngoingClick = onOngoingClick,
                onAnnouncedClick = onAnnouncedClick,
                onSearchClick = onSearchClick
            )
        } else {
            SearchField(
                text = searchText,
                onTextChange = {
                    searchText = it
                    onSearchTextChange(it)
                },
                onCancelClick = onCancelSearchClick
            )
        }
    }
}

// Long because it lays out four sibling bindings (filter spacer, two tabs, divider, search
// button) each with its own exact spacing/color, not because of nested logic.
@Suppress("FunctionNaming", "LongMethod")
@Composable
private fun TabsRow(
    selectedSection: SectionHatUi,
    onOngoingClick: () -> Unit,
    onAnnouncedClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val amiko = FontFamily(Font(CelebrityRes.font.amiko))
    val tabShadow = Shadow(
        color = Color.Black,
        offset = Offset(x = -4f, y = 4f),
        blurRadius = 4f
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(top = 8.dp)
    ) {
        // Reserved layout space with no interactive control.
        Spacer(Modifier.padding(start = 8.dp).size(TOP_BAR_CONTROL_SIZE_DP.dp))

        Text(
            text = stringResource(Res.string.on_air),
            color = if (selectedSection == SectionHatUi.ONGOINGS) Cinnabar500 else WhiteTransparent,
            fontFamily = amiko,
            fontSize = LARGE_ACCENT_TEXT_SP,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = TextStyle(shadow = tabShadow),
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp)
                .clickable(onClick = onOngoingClick)
                .testTag("ongoing_button")
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(width = 3.dp, height = 9.dp)
                .shadow(2.dp)
                .background(WhiteTransparent)
        )

        Text(
            text = stringResource(Res.string.soon),
            color = if (selectedSection == SectionHatUi.ANNOUNCED) Cinnabar500 else WhiteTransparent,
            fontFamily = amiko,
            fontSize = LARGE_ACCENT_TEXT_SP,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = TextStyle(shadow = tabShadow),
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp)
                .clickable(onClick = onAnnouncedClick)
        )

        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(width = TOP_BAR_CONTROL_SIZE_DP.dp, height = TOP_BAR_CONTROL_SIZE_DP.dp),
            contentAlignment = Alignment.Center
        ) {
            // Manual hard-edged shadow: a second, larger, darker icon behind the real one.
            Image(
                painter = painterResource(Res.drawable.ic_search_34),
                contentDescription = null,
                colorFilter = ColorFilter.tint(BlackTransparent),
                modifier = Modifier.size(34.dp)
            )
            IconButton(onClick = onSearchClick, modifier = Modifier.size(TOP_BAR_CONTROL_SIZE_DP.dp)) {
                Image(
                    painter = painterResource(Res.drawable.ic_search_32),
                    contentDescription = stringResource(Res.string.search_on_description),
                    colorFilter = ColorFilter.tint(
                        if (selectedSection == SectionHatUi.SEARCH) Cinnabar500 else WhiteTransparent
                    ),
                    // The magnifying-glass glyph isn't centered the same way in this icon's own
                    // artwork as it is in ic_search_cancel_32's. So this nudge lines its contour
                    // up with the cancel icon shown in the exact same spot once search opens.
                    modifier = Modifier.size(32.dp).offset(x = SEARCH_ICON_OFFSET_X_DP, y = SEARCH_ICON_OFFSET_Y_DP)
                )
            }
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun SearchField(text: String, onTextChange: (String) -> Unit, onCancelClick: () -> Unit) {
    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            if (newText.length <= SEARCH_TEXT_MAX_LENGTH) {
                onTextChange(newText)
            }
        },
        placeholder = {
            Text(
                text = stringResource(Res.string.search_hint),
                fontSize = SUBTITLE1_SP,
                fontWeight = FontWeight.Normal
            )
        },
        singleLine = true,
        textStyle = TextStyle(color = White),
        // A trailing icon slot, not a Box overlaid on top of the field: the field's own
        // decoration box reserves room for it on every line, so typed text and the (possibly
        // wrapped) placeholder can never render underneath it, at any font/display scale.
        trailingIcon = {
            IconButton(onClick = onCancelClick, modifier = Modifier.size(TOP_BAR_CONTROL_SIZE_DP.dp)) {
                Image(
                    painter = painterResource(Res.drawable.ic_search_cancel_32),
                    contentDescription = stringResource(Res.string.search_off_description),
                    colorFilter = ColorFilter.tint(Cinnabar500),
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Cinnabar500,
            unfocusedBorderColor = Cinnabar500,
            focusedContainerColor = BlackTransparent,
            unfocusedContainerColor = BlackTransparent,
            focusedPlaceholderColor = WhiteTransparent,
            unfocusedPlaceholderColor = WhiteTransparent,
            focusedTextColor = White,
            unfocusedTextColor = White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 8.dp)
            // A minimum, not a fixed height: at large font/display scales the placeholder
            // can need more than one line, and this lets the field grow to fit it instead of
            // clipping it.
            .heightIn(min = TOP_BAR_CONTROL_SIZE_DP.dp)
            .shadow(1.dp)
    )
}

@Suppress("FunctionNaming", "UnusedPrivateMember")
@Preview(widthDp = TOP_BAR_PREVIEW_WIDTH_DP, heightDp = TOP_BAR_PREVIEW_HEIGHT_DP)
@Composable
private fun AnimeListTopBarTabsPreview() {
    AnotiTheme {
        AnimeListTopBar(
            selectedSection = SectionHatUi.ONGOINGS,
            search = SearchUi.HIDDEN,
            onOngoingClick = {},
            onAnnouncedClick = {},
            onSearchClick = {},
            onCancelSearchClick = {},
            onSearchTextChange = {}
        )
    }
}

@Suppress("FunctionNaming", "UnusedPrivateMember")
@Preview(widthDp = TOP_BAR_PREVIEW_WIDTH_DP, heightDp = TOP_BAR_PREVIEW_HEIGHT_DP)
@Composable
private fun AnimeListTopBarSearchPreview() {
    AnotiTheme {
        AnimeListTopBar(
            selectedSection = SectionHatUi.SEARCH,
            search = SearchUi.SHOWN,
            onOngoingClick = {},
            onAnnouncedClick = {},
            onSearchClick = {},
            onCancelSearchClick = {},
            onSearchTextChange = {}
        )
    }
}
