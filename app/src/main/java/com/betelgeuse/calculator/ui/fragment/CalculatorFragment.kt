package com.betelgeuse.calculator.ui.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroupOverlay
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.betelgeuse.calculator.R
import com.betelgeuse.calculator.ui.widget.view.CalculatorEditText
import com.betelgeuse.calculator.util.CalculatorExpressionBuilder
import com.betelgeuse.calculator.util.CalculatorState
import com.betelgeuse.calculator.util.animation.RevealAnimator
import com.betelgeuse.calculator.util.animation.TextSliderAnimator
import kotlinx.android.synthetic.main.fragment_calculator.*
import kotlinx.android.synthetic.main.layout_arithmetic_pad.*
import kotlinx.android.synthetic.main.layout_display.*
import kotlinx.android.synthetic.main.layout_number_pad.*
import kotlinx.android.synthetic.main.pad_advanced.*


class CalculatorFragment : Fragment(), CalculatorEditText.OnTextSizeChangeListener/*, View.OnLongClickListener*/ {

    private lateinit var viewModel: CalculatorViewModel
    private var buttonPressListener = ButtonPressListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CalculatorViewModel::class.java)

        viewModel.currentStateLiveData.observe(this, Observer {
            if (it != null) {
                when (it) {
                    CalculatorState.RESULT -> {
                        deleteButton.visibility = View.INVISIBLE
                        clearButton.visibility = View.VISIBLE
                    }
                    CalculatorState.ERROR -> {
                        deleteButton.visibility = View.INVISIBLE
                        clearButton.visibility = View.VISIBLE

                        val errorColor = getColor(R.color.calculator_error_color)

                        formulaEditText.setTextColor(errorColor)
                        resultEditText.setTextColor(errorColor)
                        activity?.window?.statusBarColor = errorColor
                    }
                    else -> {
                        deleteButton.visibility = View.VISIBLE
                        clearButton.visibility = View.INVISIBLE

                        formulaEditText.setTextColor(getColor(R.color.display_formula_text_color))
                        resultEditText.setTextColor(getColor(R.color.display_result_text_color))

                        activity?.window?.statusBarColor = getColor(R.color.colorPrimaryDark)
                    }
                }
            }
        })

        observeErrorResult()
        observeResult()
        observeDeleteFormulaLine()
    }

    private fun observeDeleteFormulaLine() {
        viewModel.removeLineLiveData.observe(this, Observer {
            if (it != null)
                formulaEditText.text?.delete(it.first, it.second)
            if (formulaEditText.text?.isEmpty() == true)
                resultEditText.text?.clear()
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_calculator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.setBackgroundResource(R.drawable.fab_background)
        fab.setOnClickListener {
            if (viewModel.currentStateLiveData.value == CalculatorState.INPUT) {
                viewModel.currentStateLiveData.value = CalculatorState.EVALUATE
                viewModel.evaluateFormula(formulaEditText.text!!)
            }
        }
        setupButtonListener()
        formulaEditText.setOnTextSizeChangeListener(this)
        formulaEditText.setEditableFactory(object : Editable.Factory() {
            override fun newEditable(source: CharSequence): Editable {
                val isEdited = viewModel.currentStateLiveData.value == CalculatorState.INPUT || viewModel.currentStateLiveData.value == CalculatorState.ERROR
                return CalculatorExpressionBuilder(source, viewModel.tokenizer, isEdited)
            }
        })
        formulaEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                viewModel.currentStateLiveData.value = CalculatorState.INPUT
                viewModel.evaluateFormula(editable)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })
    }

    private fun setupButtonListener() {
        oneButton.setOnClickListener(buttonPressListener)
        twoButton.setOnClickListener(buttonPressListener)
        threeButton.setOnClickListener(buttonPressListener)
        fourButton.setOnClickListener(buttonPressListener)
        fiveButton.setOnClickListener(buttonPressListener)
        sixButton.setOnClickListener(buttonPressListener)
        sevenButton.setOnClickListener(buttonPressListener)
        eightButton.setOnClickListener(buttonPressListener)
        nineButton.setOnClickListener(buttonPressListener)
        dotButton.setOnClickListener(buttonPressListener)
        zeroButton.setOnClickListener(buttonPressListener)
        deleteButton.setOnClickListener(buttonPressListener)

        sinButton.setOnClickListener(buttonPressListener)
        cosButton.setOnClickListener(buttonPressListener)
        tanButton.setOnClickListener(buttonPressListener)
        lnButton.setOnClickListener(buttonPressListener)
        logButton.setOnClickListener(buttonPressListener)
        factorialButton.setOnClickListener(buttonPressListener)
        leftParenthesisButton.setOnClickListener(buttonPressListener)
        rightParenthesisButton.setOnClickListener(buttonPressListener)
        squareRootButton.setOnClickListener(buttonPressListener)
        piButton.setOnClickListener(buttonPressListener)
        eButton.setOnClickListener(buttonPressListener)
        powerButton.setOnClickListener(buttonPressListener)

        addButton.setOnClickListener(buttonPressListener)
        subtractButton.setOnClickListener(buttonPressListener)
        multiplyButton.setOnClickListener(buttonPressListener)
        divideButton.setOnClickListener(buttonPressListener)

        clearButton.setOnClickListener(buttonPressListener)

        deleteButton.setOnLongClickListener {
            onClear()
            return@setOnLongClickListener true
        }
    }

    private var currentAnimator: Animator? = null

    private fun observeErrorResult() {
        viewModel.errorResultLiveData.observe(this, Observer {
            if (it != null) {
                if (viewModel.currentStateLiveData.value == CalculatorState.EVALUATE) {
                    val displayViewRect = Rect()
                    displayViewHolder.getGlobalVisibleRect(displayViewRect)
                    val revealAnimator = RevealAnimator(fab, displayViewRect, getColor(R.color.calculator_error_color))
                    revealAnimator.interpolator = AccelerateDecelerateInterpolator()
                    revealAnimator.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator?) {
                            currentAnimator = animation
                            viewModel.currentStateLiveData.value = CalculatorState.ERROR
                            resultEditText.setText(it)
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            currentAnimator = null
                        }
                    })
                    revealAnimator.groupOverlay = activity?.window?.decorView?.overlay as ViewGroupOverlay ?: return@Observer
                    revealAnimator.start()
                } else {
                    resultEditText.text?.clear()
                }
            }
        })
    }

    private fun observeResult() {
        viewModel.resultLiveData.observe(this, Observer {
            if (it != null) {
                when (viewModel.currentStateLiveData.value) {
                    CalculatorState.INPUT -> resultEditText.setText(it)
                    else -> {

                        val textSliderAnimator = TextSliderAnimator(resultEditText, formulaEditText)
                        textSliderAnimator.duration = activity?.resources!!.getInteger(android.R.integer.config_longAnimTime).toLong()
                        textSliderAnimator.interpolator = AccelerateDecelerateInterpolator()
                        textSliderAnimator.animationText = it
                        textSliderAnimator.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationStart(animation: Animator?) {
                                currentAnimator = animation
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                viewModel.currentStateLiveData.value = CalculatorState.RESULT
                                currentAnimator = null
                            }
                        })
                        textSliderAnimator.start()
                    }
                }
            }
        })
    }

    override fun onTextSizeChanged(textView: TextView, oldSize: Float) {
        if (viewModel.currentStateLiveData.value !== CalculatorState.INPUT) {
            // Only animate text changes that occur from user input.
            return
        }

        // Calculate the values needed to perform the scale and translation animations,
        // maintaining the same apparent baseline for the displayed text.
        val textScale = oldSize / textView.textSize
        val translationX = (1.0f - textScale) * (textView.width / 2.0f - textView.paddingEnd)
        val translationY = (1.0f - textScale) * (textView.height / 2.0f - textView.paddingBottom)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(textView, View.SCALE_X, textScale, 1.0f),
                ObjectAnimator.ofFloat(textView, View.SCALE_Y, textScale, 1.0f),
                ObjectAnimator.ofFloat(textView, View.TRANSLATION_X, translationX, 0.0f),
                ObjectAnimator.ofFloat(textView, View.TRANSLATION_Y, translationY, 0.0f))
        animatorSet.duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.start()
    }

    private fun getColor(coloResId: Int) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        resources.getColor(coloResId, activity?.theme!!)
    } else {
        @Suppress("DEPRECATION")
        resources.getColor(coloResId)
    }

    private fun onClear() {
        if (TextUtils.isEmpty(formulaEditText.text)) {
            return
        }
        val sourceView = if (clearButton.isVisible) {
            clearButton
        } else {
            deleteButton
        }
        val displayViewRect = Rect()
        displayViewHolder.getGlobalVisibleRect(displayViewRect)
        val revealAnimator = RevealAnimator(sourceView, displayViewRect, getColor(R.color.colorAccent))
        revealAnimator.interpolator = AccelerateDecelerateInterpolator()
        revealAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                formulaEditText.editableText.clear()
                resultEditText.editableText.clear()
                viewModel.resultLiveData.value = null
                currentAnimator = animation
            }

            override fun onAnimationEnd(animation: Animator?) {
                currentAnimator = null
            }
        })
        revealAnimator.groupOverlay = activity?.window?.decorView?.overlay as ViewGroupOverlay ?: return
        revealAnimator.start()
    }

    inner class ButtonPressListener : View.OnClickListener {
        override fun onClick(view: View) {
            if (view is Button) {
                when (view.id) {
                    R.id.deleteButton -> viewModel.onDelete(formulaEditText.editableText)
                    R.id.clearButton -> onClear()
                    R.id.sinButton, R.id.cosButton, R.id.tanButton, R.id.lnButton, R.id.logButton ->
                        // Add left parenthesis after functions.
                        formulaEditText.append("${view.text}(")
                    else -> formulaEditText.append(view.text)
                }
            }
        }
    }
}
