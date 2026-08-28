package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.model.SectionUi
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.model.UiModel
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.generated.resources.favorites
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.generated.resources.ic_favorite_24
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.generated.resources.ic_main_24
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.generated.resources.main
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.AnotiTheme
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Black
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.CAPTION_SP
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Cinnabar500
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Cinnabar500Transparent
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.NAV_BAR_ELEVATION_DP
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.NAV_BAR_ICON_SIZE_DP
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.SilverTransparent
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * The app's bottom navigation bar: a Main/Favorites tab switcher with a badge on Favorites.
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
fun BottomNavigationBar(
    uiModel: UiModel,
    dispatch: (BottomNavigationBarStore.Intent) -> Unit
) {
    // NavigationBar owns exactly one inset-consuming layer: its own windowInsets is disabled
    // (fixed at zero) so its content is pinned to the original's exact 56dp, and the Spacer below
    // reserves the system navigation bar's own height with matching background — one bar, split
    // into a fixed-size content region plus a system-inset region, not two components each trying
    // to pad for the same inset (that double-padding is exactly what the original View-based bar
    // avoided by being the only one that consumed it).
    Column(modifier = Modifier.shadow(NAV_BAR_ELEVATION_DP)) {
        NavigationBar(
            containerColor = Black,
            windowInsets = WindowInsets(0.dp),
            modifier = Modifier.height(NAV_BAR_HEIGHT_DP)
        ) {
            NavigationBarItem(
                selected = uiModel.selectedSection == SectionUi.MAIN,
                onClick = { dispatch(BottomNavigationBarStore.Intent.MainSectionClick) },
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_main_24),
                        contentDescription = null,
                        modifier = Modifier.size(NAV_BAR_ICON_SIZE_DP)
                    )
                },
                label = { NavigationBarLabel(stringResource(Res.string.main)) },
                alwaysShowLabel = true,
                colors = navigationBarItemColors()
            )
            NavigationBarItem(
                selected = uiModel.selectedSection == SectionUi.FAVORITES,
                onClick = { dispatch(BottomNavigationBarStore.Intent.FavoritesSectionClick) },
                icon = { FavoritesIcon(favoritesBadgeNumber = uiModel.favoritesBadgeNumber) },
                label = { NavigationBarLabel(stringResource(Res.string.favorites)) },
                alwaysShowLabel = true,
                colors = navigationBarItemColors()
            )
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .background(Black)
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
        )
    }
}

@Suppress("FunctionNaming")
@Composable
private fun NavigationBarLabel(text: String) {
    Text(text = text, fontSize = CAPTION_SP, fontWeight = FontWeight.Normal)
}

@Suppress("FunctionNaming")
@Composable
private fun FavoritesIcon(favoritesBadgeNumber: Int) {
    BadgedBox(
        badge = {
            if (favoritesBadgeNumber > 0) {
                Badge(containerColor = Cinnabar500Transparent, contentColor = Black) {
                    Text(text = formatBadgeNumber(favoritesBadgeNumber))
                }
            }
        }
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_favorite_24),
            contentDescription = null,
            modifier = Modifier.size(NAV_BAR_ICON_SIZE_DP)
        )
    }
}

@Composable
private fun navigationBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Cinnabar500,
    selectedTextColor = Cinnabar500,
    unselectedIconColor = SilverTransparent,
    unselectedTextColor = SilverTransparent,
    indicatorColor = Color.Transparent
)

// Matches BadgeDrawable's default maxCharacterCount = 4 behavior: cap the displayed number at
// 9999, show "9999+" above that, instead of an ever-growing digit string.
private fun formatBadgeNumber(number: Int): String =
    if (number > BADGE_MAX_NUMBER) "$BADGE_MAX_NUMBER+" else number.toString()

private const val BADGE_MAX_NUMBER = 9999

private val NAV_BAR_HEIGHT_DP = 56.dp

private val previewMainSelected =
    UiModel(selectedSection = SectionUi.MAIN, favoritesBadgeNumber = 0)
private val previewFavoritesSelectedWithBadge =
    UiModel(selectedSection = SectionUi.FAVORITES, favoritesBadgeNumber = 3)

@Suppress("FunctionNaming", "UnusedPrivateMember")
@Preview
@Composable
private fun BottomNavigationBarMainSelectedPreview() {
    AnotiTheme {
        BottomNavigationBar(uiModel = previewMainSelected, dispatch = {})
    }
}

@Suppress("FunctionNaming", "UnusedPrivateMember")
@Preview
@Composable
private fun BottomNavigationBarFavoritesSelectedWithBadgePreview() {
    AnotiTheme {
        BottomNavigationBar(uiModel = previewFavoritesSelectedWithBadge, dispatch = {})
    }
}
