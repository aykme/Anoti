package com.alekseivinogradov.celebrity.impl.presentation.repeat_listener

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View

class RepeatListener(
    private val initialInterval: Long,
    private val repeatInterval: Long,
    private val clickListener: View.OnClickListener
) : View.OnTouchListener {

    private val handler = Handler(Looper.getMainLooper())
    private var touchedView: View? = null
    private lateinit var handlerRunnable: Runnable

    init {
        if (initialInterval < 0 || repeatInterval <= 0)
            throw IllegalArgumentException("Incorrect intervals")

        handlerRunnable = Runnable {
            if (touchedView?.isPressed == true) {
                touchedView?.let { view: View ->
                    clickListener.onClick(view)
                }
                handler.postDelayed(
                    /* r = */ handlerRunnable,
                    /* delayMillis = */ repeatInterval
                )
            } else {
                handler.removeCallbacks(handlerRunnable)
                touchedView?.isPressed = false
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        if (view == null || motionEvent == null) {
            handler.removeCallbacks(handlerRunnable)
            return false
        } else {
            touchedView = view
        }

        return when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                handler.removeCallbacks(handlerRunnable)
                handler.postDelayed(
                    /* r = */ handlerRunnable,
                    /* delayMillis = */ initialInterval
                )
                touchedView?.isPressed = true
                clickListener.onClick(view)
                true
            }

            MotionEvent.ACTION_UP -> {
                handler.removeCallbacks(handlerRunnable)
                touchedView?.isPressed = false
                true
            }

            else -> false
        }
    }
}
