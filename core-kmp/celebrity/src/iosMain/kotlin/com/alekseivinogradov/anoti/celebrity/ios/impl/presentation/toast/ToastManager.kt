package com.alekseivinogradov.anoti.celebrity.ios.impl.presentation.toast

import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.connection_error
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.unknown_error
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderKmp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UILabel
import platform.UIKit.UIView
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

/**
 * Shows an error message in a label laid over the key window. iOS has no system toast, so the
 * view is built and torn down here.
 */
object ToastManager {

    private val scope by lazy {
        CoroutineScope(CoroutineContextProviderKmp().mainCoroutineContext)
    }

    private var job: Job? = null

    private var toastView: UIView? = null

    fun makeConnectionErrorToast() {
        job?.cancel()
        job = scope.launch {
            showToast(getString(Res.string.connection_error))
        }
    }

    fun makeUnknownErrorToast() {
        job?.cancel()
        job = scope.launch {
            showToast(getString(Res.string.unknown_error))
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun showToast(text: String) {
        val window = keyWindow() ?: return
        // A previous message would otherwise stay on screen underneath this one.
        toastView?.removeFromSuperview()

        val label = UILabel().apply {
            setText(text)
            setTextColor(UIColor.whiteColor)
            setBackgroundColor(UIColor.blackColor.colorWithAlphaComponent(TOAST_BACKGROUND_ALPHA))
            setTextAlignment(NSTextAlignmentCenter)
            setNumberOfLines(0)
            setTranslatesAutoresizingMaskIntoConstraints(false)
            layer.cornerRadius = TOAST_CORNER_RADIUS
            layer.masksToBounds = true
        }
        window.addSubview(label)
        toastView = label

        NSLayoutConstraint.activateConstraints(
            listOf(
                label.centerXAnchor.constraintEqualToAnchor(window.centerXAnchor),
                label.bottomAnchor.constraintEqualToAnchor(
                    anchor = window.safeAreaLayoutGuide.bottomAnchor,
                    constant = -TOAST_BOTTOM_INSET
                ),
                label.widthAnchor.constraintLessThanOrEqualToAnchor(
                    anchor = window.widthAnchor,
                    multiplier = TOAST_MAX_WIDTH_FRACTION
                )
            )
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun keyWindow(): UIWindow? = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .flatMap { scene: UIWindowScene -> scene.windows.filterIsInstance<UIWindow>() }
        .firstOrNull { window: UIWindow -> window.isKeyWindow() }
}

private const val TOAST_BACKGROUND_ALPHA = 0.8
private const val TOAST_CORNER_RADIUS = 8.0
private const val TOAST_BOTTOM_INSET = 64.0
private const val TOAST_MAX_WIDTH_FRACTION = 0.9
