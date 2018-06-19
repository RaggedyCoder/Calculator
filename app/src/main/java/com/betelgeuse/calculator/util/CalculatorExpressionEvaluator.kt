package com.betelgeuse.calculator.util

import android.util.Log
import com.betelgeuse.calculator.R
import com.betelgeuse.calculator.util.text.CalculatorExpressionTokenizer

import org.javia.arity.Symbols
import org.javia.arity.SyntaxException
import org.javia.arity.Util
import java.lang.NumberFormatException

class CalculatorExpressionEvaluator(private val mTokenizer: CalculatorExpressionTokenizer) {

    private val mSymbols: Symbols = Symbols()

    fun evaluate(expr: CharSequence, callback: EvaluateCallback) {
        evaluate(expr.toString(), callback)
    }

    private fun evaluate(expr: String, callback: EvaluateCallback) {
        var expr = expr
        expr = mTokenizer.getNormalizedExpression(expr)

        // remove any trailing operators
        while (expr.isNotEmpty() && "+-/*".indexOf(expr[expr.length - 1]) != -1) {
            expr = expr.substring(0, expr.length - 1)
        }

        try {
            if (expr.isEmpty() || java.lang.Double.valueOf(expr) != null) {
                callback.onEvaluate(expr, null, Constants.INVALID_RES_ID)
                return
            }
        } catch (e: NumberFormatException) {
            // expr is not a simple number
        }

        try {
            val result = mSymbols.eval(expr)
            if (java.lang.Double.isNaN(result)) {
                callback.onEvaluate(expr, null, R.string.error_nan)
            } else {
                // The arity library uses floating point arithmetic when evaluating the expression
                // leading to precision errors in the result. The method doubleToString hides these
                // errors; rounding the result by dropping N digits of precision.
                val resultString = mTokenizer.getLocalizedExpression(
                        Util.doubleToString(result, MAX_DIGITS, ROUNDING_DIGITS))
                callback.onEvaluate(expr, resultString, Constants.INVALID_RES_ID)
            }
        } catch (e: SyntaxException) {
            Log.d("TAG", "$e")
            callback.onEvaluate(expr, null, R.string.error_syntax)
        }

    }

    interface EvaluateCallback {
        fun onEvaluate(expr: String, result: String?, errorResourceId: Int)
    }

    companion object {

        /**
         * The maximum number of significant digits to display.
         */
        private const val MAX_DIGITS = 12

        /**
         * A [Double] has at least 17 significant digits, we show the first [.MAX_DIGITS]
         * and use the remaining digits as guard digits to hide floating point precision errors.
         */
        private val ROUNDING_DIGITS = Math.max(17 - MAX_DIGITS, 0)
    }
}