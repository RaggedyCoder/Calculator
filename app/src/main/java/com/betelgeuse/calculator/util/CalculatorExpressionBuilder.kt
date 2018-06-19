package com.betelgeuse.calculator.util

import android.text.SpannableStringBuilder
import android.text.TextUtils
import com.betelgeuse.calculator.util.text.CalculatorExpressionTokenizer

class CalculatorExpressionBuilder(
        text: CharSequence, private val mTokenizer: CalculatorExpressionTokenizer, private var mIsEdited: Boolean) : SpannableStringBuilder(text) {

    override fun replace(start: Int, end: Int, tb: CharSequence, tbstart: Int,
                         tbend: Int): SpannableStringBuilder {
        var tempStart = start
        if (tempStart != length || end != length) {
            mIsEdited = true
            return super.replace(tempStart, end, tb, tbstart, tbend)
        }

        var appendExpr = mTokenizer.getNormalizedExpression(tb.subSequence(tbstart, tbend).toString())
        if (appendExpr.length == 1) {
            val expr = mTokenizer.getNormalizedExpression(toString())
            when (appendExpr[0]) {
                ')' -> {
                    if (tempStart == 0) {
                        appendExpr = ""
                    } else if (expr.lastIndexOf('(') == tempStart) {
                        appendExpr = ""
                    } else if (!isRightParenthesisAllowed(expr)) {
                        appendExpr = ""
                    }
                }
                '.' -> {
                    // don't allow two decimals in the same number
                    val index = expr.lastIndexOf('.')
                    if (index != -1 && TextUtils.isDigitsOnly(expr.substring(index + 1, tempStart))) {
                        appendExpr = ""
                    }
                }
                '+', '*', '/' -> {
                    // don't allow leading operator
                    if (tempStart == 0) {
                        appendExpr = ""
                    } else {
                        // don't allow multiple successive operators
                        while (tempStart > 0 && "+-*/".indexOf(expr[tempStart - 1]) != -1) {
                            --tempStart
                        }
                        // don't allow -- or +-
                        if (tempStart > 0 && "+-".indexOf(expr[tempStart - 1]) != -1) {
                            --tempStart
                        }

                        // mark as edited since operators can always be appended
                        mIsEdited = true
                        tempStart = minusCheck(tempStart, expr)
                    }
                }
                '-' -> {
                    tempStart = minusCheck(tempStart, expr)
                }
                else -> {
                }
            }
        }

        // since this is the first edit replace the entire string
        if (!mIsEdited && appendExpr.isNotEmpty()) {
            tempStart = 0
            mIsEdited = true
        }

        appendExpr = mTokenizer.getLocalizedExpression(appendExpr)
        return super.replace(tempStart, end, appendExpr, 0, appendExpr.length)
    }

    private fun isRightParenthesisAllowed(expr: String): Boolean {
        var count = 0
        expr.forEach {
            if (it == '(')
                count++
            else if (it == ')')
                count--
        }
        return count > 0
    }

    private fun minusCheck(start: Int, expr: String): Int {
        var tempStart = start
        if (tempStart > 0 && "+-".indexOf(expr[tempStart - 1]) != -1) {
            --tempStart
        }
        mIsEdited = true
        return tempStart
    }
}