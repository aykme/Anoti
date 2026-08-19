package com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.loading_img
import org.jetbrains.compose.resources.painterResource

/**
 * A continuously spinning loading indicator — the Compose equivalent of the classic View
 * system's `android:animated-rotate` drawable, which Compose Resources doesn't support.
 *
 * @param contentDescription accessibility description; `null` marks the spinner as decorative.
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
fun LoadingSpinner(modifier: Modifier = Modifier, contentDescription: String? = null) {
    val infiniteTransition = rememberInfiniteTransition(label = "loadingSpinner")
    val rotationDegrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = FULL_ROTATION_DEGREES,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = ROTATION_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loadingSpinnerRotation"
    )
    Image(
        painter = painterResource(Res.drawable.loading_img),
        contentDescription = contentDescription,
        modifier = modifier.graphicsLayer { rotationZ = rotationDegrees }
    )
}

private const val FULL_ROTATION_DEGREES = 360f

// Best-guess placeholder matching the original animated-rotate drawable's approximate speed;
// not confirmed against the real device animation.
private const val ROTATION_DURATION_MS = 1000
