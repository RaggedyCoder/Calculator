package com.betelgeuse.calculator.util.animation

import android.animation.*
import android.view.View
import com.betelgeuse.calculator.ui.widget.view.CalculatorEditText

class TextSliderAnimator(private val startingTextView: CalculatorEditText, private val destinationTextView: CalculatorEditText) : Animator() {

    private var animatorSet = AnimatorSet()

    var animationText: CharSequence = ""
    private var listener: AnimatorListener? = null

    private fun startAnimation() {

        // Calculate the values needed to perform the scale and translation animations,
        // accounting for how the scale will affect the final position of the text.
        val resultScale = destinationTextView.getVariableTextSize(animationText.toString()) / startingTextView.textSize
        val resultTranslationX = (1.0f - resultScale) * (startingTextView.width / 2.0f - startingTextView.paddingEnd)
        val resultTranslationY = (1.0f - resultScale) *
                (startingTextView.height / 2.0f - startingTextView.paddingBottom) +
                (destinationTextView.bottom - startingTextView.bottom) +
                (startingTextView.paddingBottom - destinationTextView.paddingBottom)
        val formulaTranslationY = -destinationTextView.bottom.toFloat()

        // Use a value animator to fade to the final text color over the course of the animation.
        val resultTextColor = startingTextView.currentTextColor
        val formulaTextColor = destinationTextView.currentTextColor
        val textColorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), resultTextColor, formulaTextColor)
        textColorAnimator.addUpdateListener { valueAnimator -> startingTextView.setTextColor(valueAnimator.animatedValue as Int) }

        animatorSet.playTogether(
                textColorAnimator,
                ObjectAnimator.ofFloat(startingTextView, View.SCALE_X, resultScale),
                ObjectAnimator.ofFloat(startingTextView, View.SCALE_Y, resultScale),
                ObjectAnimator.ofFloat(startingTextView, View.TRANSLATION_X, resultTranslationX),
                ObjectAnimator.ofFloat(startingTextView, View.TRANSLATION_Y, resultTranslationY),
                ObjectAnimator.ofFloat(destinationTextView, View.TRANSLATION_Y, formulaTranslationY))

        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                startingTextView.setText(animationText)
                listener?.onAnimationStart(animation)
            }

            override fun onAnimationEnd(animation: Animator) {

                // Reset all of the values modified during the animation.
                startingTextView.setTextColor(resultTextColor)
                startingTextView.scaleX = 1.0f
                startingTextView.scaleY = 1.0f
                startingTextView.translationX = 0.0f
                startingTextView.translationY = 0.0f
                destinationTextView.translationY = 0.0f

                // Finally update the formula to use the current result.
                destinationTextView.setText(animationText)
                startingTextView.editableText.clear()
                listener?.onAnimationEnd(animation)
            }
        })
        animatorSet.start()
    }

    override fun addListener(listener: AnimatorListener) {
        this.listener = listener
    }

    override fun start() {
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