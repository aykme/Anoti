package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.SEARCH_TEXT_MAX_LENGTH
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.TOP_BAR_CONTROL_SIZE_DP
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.TOP_BAR_ICON_OFFSET_X_DP
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.TOP_BAR_ICON_OFFSET_Y_DP
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.TOP_BAR_ICON_SHADOW_OFFSET_X_DP
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.TOP_BAR_PREVIEW_HEIGHT_DP
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.TOP_BAR_PREVIEW_WIDTH_DP
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.SearchUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.SectionHatUi
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.ic_info_outline_28
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.ic_info_outline_30
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.ic_search_32
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.ic_search_34
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.ic_search_cancel_32
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.info_description
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.on_air
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.search_hint
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.search_off_description
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.search_on_description
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.soon
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.BlackTransparent
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Cinnabar500
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.LARGE_ACCENT_TEXT_SP
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.SUBTITLE1_SP
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.White
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.WhiteTransparent
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.amiko
import com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.compose.AnotiTheme
import com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.compose.systemBarsTopPadding
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
    onSearchTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Hoisted above the `search == SHOWN` check so typed text survives closing and reopening
    // the search bar. Saveable so it also survives process death: every keystroke pushes the
    // same value into this field's own SavedStateRegistry entry and, via onTextChange, into the
    // domain search text that NavAnimeListScreenComponent.applyRestoredMainState restores — the
    // two are independent persistence paths, kept in sync only because both capture the same
    // value on every keystroke, not because one reads from the other.
    var searchText by rememberSaveable { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(search) {
        if (search == SearchUi.HIDDEN) {
            keyboardController?.hide()
        }
    }

    Box(
        modifier = modifier
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

@Suppress("FunctionNaming")
@Composable
private fun TabsRow(
    selectedSection: SectionHatUi,
    onOngoingClick: () -> Unit,
    onAnnouncedClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(top = 8.dp)
    ) {
        InfoButton(
            onClick = { println("tagtag Info") },
            modifier = Modifier.padding(start = 8.dp)
        )
        SectionTab(
            text = stringResource(Res.string.on_air),
            selected = selectedSection == SectionHatUi.ONGOINGS,
            onClick = onOngoingClick,
            modifier = Modifier
                .weight(1f)
                .testTag("ongoing_button")
        )
        TabsDivider()
        SectionTab(
            text = stringResource(Res.string.soon),
            selected = selectedSection == SectionHatUi.ANNOUNCED,
            onClick = onAnnouncedClick,
            modifier = Modifier
                .weight(1f)
                .testTag("announced_button")
        )
        SearchButton(
            selected = selectedSection == SectionHatUi.SEARCH,
            onClick = onSearchClick,
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}

@Suppress("FunctionNaming")
@Composable
private fun InfoButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(width = TOP_BAR_CONTROL_SIZE_DP.dp, height = TOP_BAR_CONTROL_SIZE_DP.dp),
        contentAlignment = Alignment.Center
    ) {
        // Manual hard-edged shadow: a second, larger, darker icon behind the real one.
        Image(
            painter = painterResource(Res.drawable.ic_info_outline_30),
            contentDescription = null,
            colorFilter = ColorFilter.tint(BlackTransparent),
            modifier = Modifier.size(30.dp).offset(x = TOP_BAR_ICON_SHADOW_OFFSET_X_DP)
        )
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(TOP_BAR_CONTROL_SIZE_DP.dp)
                .testTag("info_button")
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_info_outline_28),
                contentDescription = stringResource(Res.string.info_description),
                colorFilter = ColorFilter.tint(WhiteTransparent),
                modifier = Modifier.size(28.dp).offset(x = TOP_BAR_ICON_OFFSET_X_DP, y = TOP_BAR_ICON_OFFSET_Y_DP)
            )
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun SectionTab(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = if (selected) Cinnabar500 else WhiteTransparent,
        fontFamily = FontFamily(Font(CelebrityRes.font.amiko)),
        fontSize = LARGE_ACCENT_TEXT_SP,
        fontWeight = FontWeight.Normal,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        style = TextStyle(
            shadow = Shadow(
                color = Color.Black,
                offset = Offset(x = -4f, y = 4f),
                blurRadius = 4f
            )
        ),
        // selectable, not clickable: it tells a screen reader this is a tab and which tab is the
        // open one. Color alone conveys neither. It also precedes the padding, so the whole tab
        // answers a tap rather than only the glyphs.
        modifier = modifier
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = onClick
            )
            .padding(vertical = 12.dp)
    )
}

@Suppress("FunctionNaming")
@Composable
private fun TabsDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 3.dp, height = 9.dp)
            .shadow(2.dp)
            .background(WhiteTransparent)
    )
}

@Suppress("FunctionNaming")
@Composable
private fun SearchButton(selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(width = TOP_BAR_CONTROL_SIZE_DP.dp, height = TOP_BAR_CONTROL_SIZE_DP.dp),
        contentAlignment = Alignment.Center
    ) {
        // Manual hard-edged shadow: a second, larger, darker icon behind the real one.
        Image(
            painter = painterResource(Res.drawable.ic_search_34),
            contentDescription = null,
            colorFilter = ColorFilter.tint(BlackTransparent),
            modifier = Modifier.size(34.dp).offset(x = TOP_BAR_ICON_SHADOW_OFFSET_X_DP)
        )
        IconButton(
            onClick = onClick,
            // The third tab of the same row, so it carries the same tab semantics as the two
            // section tabs.
            modifier = Modifier
                .size(TOP_BAR_CONTROL_SIZE_DP.dp)
                .semantics {
                    role = Role.Tab
                    this.selected = selected
                }
                .testTag("search_button")
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_search_32),
                contentDescription = stringResource(Res.string.search_on_description),
                colorFilter = ColorFilter.tint(if (selected) Cinnabar500 else WhiteTransparent),
                // The magnifying-glass glyph isn't centered the same way in this icon's own
                // artwork as it is in ic_search_cancel_32's. So this nudge lines its contour up
                // with the cancel icon shown in the exact same spot once search opens.
                modifier = Modifier.size(32.dp).offset(x = TOP_BAR_ICON_OFFSET_X_DP, y = TOP_BAR_ICON_OFFSET_Y_DP)
            )
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
