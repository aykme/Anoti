package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.White
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.WhiteTransparent
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
    topInsetDp: Dp,
    onOngoingClick: () -> Unit,
    onAnnouncedClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCancelSearchClick: () -> Unit,
    onSearchTextChange: (String) -> Unit
) {
    // Hoisted above the `search == SHOWN` check so typed text survives closing and reopening
    // the search bar.
    var searchText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topInsetDp)
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
        Spacer(Modifier.padding(start = 8.dp).size(FILTER_BUTTON_SIZE_DP.dp))

        Text(
            text = stringResource(Res.string.on_air),
            color = if (selectedSection == SectionHatUi.ONGOINGS) Cinnabar500 else WhiteTransparent,
            fontFamily = amiko,
            fontSize = TAB_TEXT_SP.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = TextStyle(shadow = tabShadow),
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOngoingClick
                )
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
            fontSize = TAB_TEXT_SP.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = TextStyle(shadow = tabShadow),
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onAnnouncedClick
                )
        )

        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(width = SEARCH_BUTTON_SIZE_DP.dp, height = SEARCH_BUTTON_SIZE_DP.dp),
            contentAlignment = Alignment.Center
        ) {
            // Manual hard-edged shadow: a second, larger, darker icon behind the real one.
            Image(
                painter = painterResource(Res.drawable.ic_search_34),
                contentDescription = null,
                colorFilter = ColorFilter.tint(BlackTransparent),
                modifier = Modifier.size(34.dp)
            )
            IconButton(onClick = onSearchClick, modifier = Modifier.size(SEARCH_BUTTON_SIZE_DP.dp)) {
                Image(
                    painter = painterResource(Res.drawable.ic_search_32),
                    contentDescription = stringResource(Res.string.search_on_description),
                    colorFilter = ColorFilter.tint(
                        if (selectedSection == SectionHatUi.SEARCH) Cinnabar500 else WhiteTransparent
                    ),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun SearchField(text: String, onTextChange: (String) -> Unit, onCancelClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text(stringResource(Res.string.search_hint)) },
            singleLine = true,
            textStyle = TextStyle(color = White),
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
                .padding(top = 8.dp)
                .height(SEARCH_FIELD_HEIGHT_DP.dp)
                .shadow(1.dp)
        )
        IconButton(
            onClick = onCancelClick,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(top = 8.dp, end = 8.dp)
                .size(CANCEL_BUTTON_SIZE_DP.dp)
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_search_cancel_32),
                contentDescription = stringResource(Res.string.search_off_description),
                colorFilter = ColorFilter.tint(Cinnabar500),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

private const val FILTER_BUTTON_SIZE_DP = 56
private const val SEARCH_BUTTON_SIZE_DP = 56
private const val SEARCH_FIELD_HEIGHT_DP = 56
private const val CANCEL_BUTTON_SIZE_DP = 56
private const val TAB_TEXT_SP = 29

@Suppress("FunctionNaming", "UnusedPrivateMember")
@Preview
@Composable
private fun AnimeListTopBarTabsPreview() {
    AnotiTheme {
        AnimeListTopBar(
            selectedSection = SectionHatUi.ONGOINGS,
            search = SearchUi.HIDDEN,
            topInsetDp = 0.dp,
            onOngoingClick = {},
            onAnnouncedClick = {},
            onSearchClick = {},
            onCancelSearchClick = {},
            onSearchTextChange = {}
        )
    }
}

@Suppress("FunctionNaming", "UnusedPrivateMember")
@Preview
@Composable
private fun AnimeListTopBarSearchPreview() {
    AnotiTheme {
        AnimeListTopBar(
            selectedSection = SectionHatUi.SEARCH,
            search = SearchUi.SHOWN,
            topInsetDp = 0.dp,
            onOngoingClick = {},
            onAnnouncedClick = {},
            onSearchClick = {},
            onCancelSearchClick = {},
            onSearchTextChange = {}
        )
    }
}
