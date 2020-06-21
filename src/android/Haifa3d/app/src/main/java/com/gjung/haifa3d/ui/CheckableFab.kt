package com.gjung.haifa3d.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.Checkable
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CheckableFab @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FloatingActionButton(context, attrs, defStyleAttr), Checkable {
    private var checked: Boolean = false

    override fun isChecked(): Boolean = checked

    override fun toggle() {
        checked = !checked
    }

    override fun setChecked(checked: Boolean) {
        this.checked = checked
    }

    override fun performClick(): Boolean {
        toggle()
        return super.performClick()
    }

}