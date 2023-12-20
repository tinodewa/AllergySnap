package com.arifin.capstone.custom

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText

class CustomEditTextPassword : TextInputEditText {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                error = if (charSequence != null && !isValidPassword(charSequence.toString())) {
                    "Password must be at least 8 characters"
                } else {
                    null
                }
            }

            override fun afterTextChanged(editable: Editable?) {
                // Do nothing
            }
        })
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }
}