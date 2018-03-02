package it.diab.ui

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

import it.diab.R

class NumericKeyboardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private lateinit var mButton0: View
    private lateinit var mButton1: View
    private lateinit var mButton2: View
    private lateinit var mButton3: View
    private lateinit var mButton4: View
    private lateinit var mButton5: View
    private lateinit var mButton6: View
    private lateinit var mButton7: View
    private lateinit var mButton8: View
    private lateinit var mButton9: View
    private lateinit var mButtonDel: View

    private lateinit var mInputTextView: TextView
    private var mOnTextChanged: (String) -> Unit = {}

    val input: Int
        get() = 
            if (TextUtils.isEmpty(mInputTextView.text)) -1 
            else mInputTextView.text.toString().toInt()

    init {
        View.inflate(context, R.layout.component_numeric_keyboard, this)
        setup()
    }

    fun bindTextView(textView: TextView, onTextChanged: (String) -> Unit = {}) {
        mInputTextView = textView
        mOnTextChanged = onTextChanged
        setupKeys()
    }

    private fun setup() {
        mButton0 = findViewById(R.id.keyboard_key_0)
        mButton1 = findViewById(R.id.keyboard_key_1)
        mButton2 = findViewById(R.id.keyboard_key_2)
        mButton3 = findViewById(R.id.keyboard_key_3)
        mButton4 = findViewById(R.id.keyboard_key_4)
        mButton5 = findViewById(R.id.keyboard_key_5)
        mButton6 = findViewById(R.id.keyboard_key_6)
        mButton7 = findViewById(R.id.keyboard_key_7)
        mButton8 = findViewById(R.id.keyboard_key_8)
        mButton9 = findViewById(R.id.keyboard_key_9)
        mButtonDel = findViewById(R.id.keyboard_key_del)
    }

    private fun setupKeys() {
        mButton0.setOnClickListener { _ -> input(0) }
        mButton1.setOnClickListener { _ -> input(1) }
        mButton2.setOnClickListener { _ -> input(2) }
        mButton3.setOnClickListener { _ -> input(3) }
        mButton4.setOnClickListener { _ -> input(4) }
        mButton5.setOnClickListener { _ -> input(5) }
        mButton6.setOnClickListener { _ -> input(6) }
        mButton7.setOnClickListener { _ -> input(7) }
        mButton8.setOnClickListener { _ -> input(8) }
        mButton9.setOnClickListener { _ -> input(9) }

        mButtonDel.setOnClickListener { _ -> del() }
        mButtonDel.setOnLongClickListener { _ ->
            mInputTextView.text = "0"
            true
        }
    }

    private fun input(n: Int) {
        var input = mInputTextView.text.toString()
        if (input.length > 2) {
            return
        }

        while (input.startsWith("0")) {
            input = input.substring(1, input.length)
        }
        input += n.toString()
        mInputTextView.text = input
        mOnTextChanged(input)
    }

    private fun del() {
        val input = mInputTextView.text.toString()
        if (input.length > 1) {
            mInputTextView.text = input.substring(0, input.length - 1)
        } else {
            mInputTextView.text = "0"
        }
    }
}
