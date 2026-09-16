package com.alekseivinogradov.anoti.celebrity.ios.impl.presentation.toast

import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.connection_error
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.unknown_error
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderKmp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
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
 * Shows an error message in a view laid over the key window. iOS has no system toast, so the
 * view is built, kept for [TOAST_DURATION_MILLIS] and removed here.
 */
object ToastManager {

    private val scope by lazy {
        CoroutineScope(CoroutineContextProviderKmp().mainCoroutineContext)
    }

    private var job: Job? = null

    private var toastView: UIView? = null

    fun makeConnectionErrorToast() {
        showError(Res.string.connection_error)
    }

    fun makeUnknownErrorToast() {
        showError(Res.string.unknown_error)
    }

    /**
     * The whole swap runs inside [scope] so that [job] and [toastView] are only ever touched on
     * the main dispatcher — callers reach this from arbitrary threads.
     */
    private fun showError(resource: StringResource) {
        scope.launch {
            job?.cancel()
            job = launch {
                val text = getString(resource)
                showToast(text)
                delay(TOAST_DURATION_MILLIS)
                removeToast()
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun showToast(text: String) {
        val window = keyWindow() ?: return
        removeToast()

        val label = UILabel().apply {
            setText(text)
            setTextColor(UIColor.whiteColor)
            setTextAlignment(NSTextAlignmentCenter)
            setNumberOfLines(0)
            setTranslatesAutoresizingMaskIntoConstraints(false)
        }
        val background = UIView().apply {
            setBackgroundColor(UIColor.blackColor.colorWithAlphaComponent(TOAST_BACKGROUND_ALPHA))
            setTranslatesAutoresizingMaskIntoConstraints(false)
            layer.cornerRadius = TOAST_CORNER_RADIUS
            layer.masksToBounds = true
        }
        background.addSubview(label)
        window.addSubview(background)
        toastView = background

        NSLayoutConstraint.activateConstraints(
            listOf(
                label.leadingAnchor.constraintEqualToAnchor(
                    anchor = background.leadingAnchor,
                    constant = TOAST_HORIZONTAL_PADDING
                ),
                label.trailingAnchor.constraintEqualToAnchor(
                    anchor = background.trailingAnchor,
                    constant = -TOAST_HORIZONTAL_PADDING
                ),
                label.topAnchor.constraintEqualToAnchor(
                    anchor = background.topAnchor,
                    constant = TOAST_VERTICAL_PADDING
                ),
                label.bottomAnchor.constraintEqualToAnchor(
                    anchor = background.bottomAnchor,
                    constant = -TOAST_VERTICAL_PADDING
                ),
                background.centerXAnchor.constraintEqualToAnchor(window.centerXAnchor),
                background.bottomAnchor.constraintEqualToAnchor(
                    anchor = window.safeAreaLayoutGuide.bottomAnchor,
                    constant = -TOAST_BOTTOM_INSET
                ),
                background.widthAnchor.constraintLessThanOrEqualToAnchor(
                    anchor = window.widthAnchor,
                    multiplier = TOAST_MAX_WIDTH_FRACTION
                )
            )
        )
    }

    private fun removeToast() {
        toastView?.removeFromSuperview()
        toastView = null
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun keyWindow(): UIWindow? = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .flatMap { scene: UIWindowScene -> scene.windows.filterIsInstance<UIWindow>() }
        .firstOrNull { window: UIWindow -> window.isKeyWindow() }
}
