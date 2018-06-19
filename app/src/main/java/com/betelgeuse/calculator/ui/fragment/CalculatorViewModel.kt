package com.betelgeuse.calculator.ui.fragment

import android.app.Application
import android.text.Editable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.betelgeuse.calculator.util.CalculatorExpressionEvaluator
import com.betelgeuse.calculator.util.CalculatorState
import com.betelgeuse.calculator.util.Constants
import com.betelgeuse.calculator.util.text.CalculatorExpressionTokenizer

class CalculatorViewModel(application: Application) : AndroidViewModel(application), CalculatorExpressionEvaluator.EvaluateCallback {

    val currentStateLiveData = object : MutableLiveData<CalculatorState>() {
        override fun setValue(value: CalculatorState?) {
            if (this.value != value)
                super.setValue(value)
        }

        override fun postValue(value: CalculatorState?) {
            if (this.value != value)
                super.postValue(value)
        }
    }
    val resultLiveData = MutableLiveData<String>()
    val errorResultLiveData = MutableLiveData<Int>()
    val removeLineLiveData = MutableLiveData<Pair<Int, Int>>()

    val tokenizer = CalculatorExpressionTokenizer(application)
    private val evaluator = CalculatorExpressionEvaluator(tokenizer)

    override fun onEvaluate(expr: String, result: String?, errorResourceId: Int) {
        when {
            !result.isNullOrBlank() -> resultLiveData.value = result
            errorResourceId != Constants.INVALID_RES_ID -> errorResultLiveData.value = errorResourceId
            currentStateLiveData.value == CalculatorState.EVALUATE -> CalculatorState.INPUT
        }
    }

    fun onDelete(formulaText: Editable) {
        val formulaLength = formulaText.length
        if (formulaLength > 0) {
            removeLineLiveData.value = when {
                formulaText.endsWith("sin(") -> formulaLength - 4 to formulaLength
                formulaText.endsWith("cos(") -> formulaLength - 4 to formulaLength
                formulaText.endsWith("tan(") -> formulaLength - 4 to formulaLength
                formulaText.endsWith("log(") -> formulaLength - 4 to formulaLength
                formulaText.endsWith("ln(") -> formulaLength - 3 to formulaLength
                else -> formulaLength - 1 to formulaLength
            }
        }
    }

    fun evaluateFormula(editable: Editable) {
        evaluator.evaluate(editable, this)
    }
}
