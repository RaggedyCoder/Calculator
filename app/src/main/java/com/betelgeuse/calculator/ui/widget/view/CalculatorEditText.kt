package com.betelgeuse.calculator.ui.widget.view

import android.content.Context
import android.graphics.Rect
import android.os.Parcelable
import android.text.TextPaint
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import com.betelgeuse.calculator.R

class CalculatorEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = android.R.attr.editTextStyle) :
        AppCompatEditText(context, attrs, defStyle) {

    private val mMaximumTextSize: Float
    private val mMinimumTextSize: Float
    private val mStepTextSize: Float

    // Temporary objects for use in layout methods.
    private val mTempPaint = TextPaint()
    private val mTempRect = Rect()

    private var mWidthConstraint = -1
    private var mOnTextSizeChangeListener: OnTextSizeChangeListener? = null

    init {

        val a = context.obtainStyledAttributes(
                attrs, R.styleable.CalculatorEditText, defStyle, 0)
        mMaximumTextSize = a.getDimension(
                R.styleable.CalculatorEditText_maxTextSize, textSize)
        mMinimumTextSize = a.getDimension(
                R.styleable.CalculatorEditText_minTextSize, textSize)
        mStepTextSize = a.getDimension(R.styleable.CalculatorEditText_stepTextSize,
                (mMaximumTextSize - mMinimumTextSize) / 3)

        a.recycle()

        customSelectionActionModeCallback = NO_SELECTION_ACTION_MODE_CALLBACK
        if (isFocusable) {
            movementMethod = ScrollingMovementMethod.getInstance()
        }
        setTextSize(TypedValue.COMPLEX_UNIT_PX, mMaximumTextSize)
        minHeight = lineHeight + compoundPaddingBottom + compoundPaddingTop
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            // Hack to prevent keyboard and insertion handle from showing.
            cancelLongPress()
        }
        return super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        mWidthConstraint = View.MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        setTextSize(TypedValue.COMPLEX_UNIT_PX, getVariableTextSize(text!!.toString()))
    }

    override fun onSaveInstanceState(): Parcelable? {
        super.onSaveInstanceState()

        // EditText will freeze any text with a selection regardless of getFreezesText() ->
        // return null to prevent any state from being preserved at the instance level.
        return null
    }

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)

        val textLength = text.length
        if (selectionStart != textLength || selectionEnd != textLength) {
            // Pin the selection to the end of the current text.
            setSelection(textLength)
        }
        setTextSize(TypedValue.COMPLEX_UNIT_PX, getVariableTextSize(text.toString()))
    }

    override fun setTextSize(unit: Int, size: Float) {
        val oldTextSize = textSize
        super.setTextSize(unit, size)

        if (mOnTextSizeChangeListener != null && textSize != oldTextSize) {
            mOnTextSizeChangeListener!!.onTextSizeChanged(this, oldTextSize)
        }
    }

    fun setOnTextSizeChangeListener(listener: OnTextSizeChangeListener) {
        mOnTextSizeChangeListener = listener
    }

    fun getVariableTextSize(text: String): Float {
        if (mWidthConstraint < 0 || mMaximumTextSize <= mMinimumTextSize) {
            // Not measured, bail early.
            return textSize
        }

        // Capture current paint state.
        mTempPaint.set(paint)

        // Step through increasing text sizes until the text would no longer fit.
        var lastFitTextSize = mMinimumTextSize
        while (lastFitTextSize < mMaximumTextSize) {
            val nextSize = Math.min(lastFitTextSize + mStepTextSize, mMaximumTextSize)
            mTempPaint.textSize = nextSize
            if (mTempPaint.measureText(text) > mWidthConstraint) {
                break
            } else {
                lastFitTextSize = nextSize
            }
        }

        return lastFitTextSize
    }

    override fun getCompoundPaddingTop(): Int {
        // Measure the top padding from the capital letter height of the text instead of the top,
        // but don't remove more than the available top padding otherwise clipping may occur.
        paint.getTextBounds("H", 0, 1, mTempRect)

        val fontMetrics = paint.fontMetricsInt
        val paddingOffset = -(fontMetrics.ascent + mTempRect.height())
        return super.getCompoundPaddingTop() - Math.min(paddingTop, paddingOffset)
    }

    override fun getCompoundPaddingBottom(): Int {
        // Measure the bottom padding from the baseline of the text instead of the bottom, but don't
        // remove more than the available bottom padding otherwise clipping may occur.
        val fontMetrics = paint.fontMetricsInt
        return super.getCompoundPaddingBottom() - Math.min(paddingBottom, fontMetrics.descent)
    }

    interface OnTextSizeChangeListener {
        fun onTextSizeChanged(textView: TextView, oldSize: Float)
    }

    companion object {

        private val NO_SELECTION_ACTION_MODE_CALLBACK = object : ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                return false
            }

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                // Prevents the selection action mode on double tap.
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {}

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }
        }
    }
}
