package com.betelgeuse.calculator.util.text

import android.content.Context
import com.betelgeuse.calculator.R
import java.text.DecimalFormatSymbols
import java.util.*

class CalculatorExpressionTokenizer(context: Context) {

    private val mReplacementMap: MutableMap<String, String>

    init {
        mReplacementMap = HashMap()

        var locale = context.resources.configuration.locale
        if (!context.resources.getBoolean(R.bool.use_localized_digits)) {
            locale = Locale.Builder()
                    .setLocale(locale)
                    .setUnicodeLocaleKeyword("nu", "latn")
                    .build()
        }

        val symbols = DecimalFormatSymbols(locale)
        val zeroDigit = symbols.zeroDigit

        mReplacementMap["."] = symbols.decimalSeparator.toString()

        for (i in 0..9) {
            mReplacementMap[Integer.toString(i)] = (i + zeroDigit.toInt()).toChar().toString()
        }

        mReplacementMap["/"] = context.getString(R.string.operator_divide)
        mReplacementMap["*"] = context.getString(R.string.operator_multiply)
        mReplacementMap["-"] = context.getString(R.string.operator_subtraction)

        mReplacementMap["cos"] = context.getString(R.string.function_cos)
        mReplacementMap["ln"] = context.getString(R.string.function_ln)
        mReplacementMap["log"] = context.getString(R.string.function_log)
        mReplacementMap["sin"] = context.getString(R.string.function_sin)
        mReplacementMap["tan"] = context.getString(R.string.function_tan)
        mReplacementMap["Infinity"] = context.getString(R.string.sign_infinity)
    }

    fun getNormalizedExpression(expr: String): String {
        var result = expr
        for ((key, value) in mReplacementMap) {
            result = result.replace(value, key)
        }
        return result
    }

    fun getLocalizedExpression(expr: String): String {
        var expr = expr
        for ((key, value) in mReplacementMap) {
            expr = expr.replace(key, value)
        }
        return expr
    }
}