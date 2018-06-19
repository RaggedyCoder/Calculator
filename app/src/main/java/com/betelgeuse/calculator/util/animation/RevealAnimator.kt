package com.betelgeuse.calculator.util.animation

import android.animation.*
import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroupOverlay


class RevealAnimator(private val sourceView: View, private val displayRect: Rect, private val color: Int) : Animator() {
    private var animatorSet = AnimatorSet()

    private var listener: Animator.AnimatorListener? = null
    var groupOverlay: ViewGroupOverlay? = null

    override fun addListener(listener: Animator.AnimatorListener) {
        this.listener = listener
    }

    private fun startAnimation() {
        val revealView = View(sourceView.context)
        revealView.bottom = displayRect.bottom
        revealView.left = displayRect.left
        revealView.right = displayRect.right
        revealView.setBackgroundColor(color)
        groupOverlay?.add(revealView)

        val clearLocation = IntArray(2)
        sourceView.getLocationInWindow(clearLocation)
        clearLocation[0] += sourceView.width / 2
        clearLocation[1] += sourceView.height / 2

        val revealCenterX = clearLocation[0] - revealView.left
        val revealCenterY = clearLocation[1] - revealView.top

        val x1_2 = Math.pow(revealView.left.toDouble() - revealCenterX, 2.0)
        val x2_2 = Math.pow(revealView.right.toDouble() - revealCenterX, 2.0)
        val y_2 = Math.pow(revealView.top.toDouble() - revealCenterY, 2.0)
        val revealRadius = Math.max(Math.sqrt(x1_2 + y_2), Math.sqrt(x2_2 + y_2)).toFloat()

        val revealAnimator = ViewAnimationUtils.createCircularReveal(revealView,
                revealCenterX, revealCenterY, 0.0f, revealRadius)
        revealAnimator.duration = Resources.getSystem().getInteger(android.R.integer.config_longAnimTime).toLong()
        val alphaAnimator = ObjectAnimator.ofFloat(revealView, View.ALPHA, 0.0f)
        alphaAnimator.duration = Resources.getSystem().getInteger(android.R.integer.config_mediumAnimTime).toLong()
        alphaAnimator.addListener(listener)

        val animatorSet = AnimatorSet()
        animatorSet.play(revealAnimator).before(alphaAnimator)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                groupOverlay?.remove(revealView)
            }
        })
        animatorSet.start()
    }

    override fun start() {
        if (groupOverlay == null)
            throw Exception("Group Overlay can't be null before starting the animation")
        startAnimation()
    }

    override fun isRunning() = animatorSet.isRunning

    override fun getDuration() = animatorSet.duration

    override fun setDuration(duration: Long): Animator {
        animatorSet.duration = duration
        return this
    }

    override fun getStartDelay() = animatorSet.startDelay

    override fun setStartDelay(startDelay: Long) {
        animatorSet.startDelay = startDelay
    }

    override fun setInterpolator(interpolator: TimeInterpolator?) {
        animatorSet.interpolator = interpolator
    }
}