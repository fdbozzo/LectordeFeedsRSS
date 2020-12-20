package com.blogspot.fdbozzo.lectorfeedsrss.ui.drawer

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ExpandableListView


class NonScrollingExpandableListView(context: Context?, attrs: AttributeSet?): ExpandableListView(context, attrs) {

    /**
     * Este método soluciona el problema de que muestra un solo grupo al mostrar por primera vez,
     * en vez de mostrar todos los que entren.
     *
     * Fuente: https://stackoverflow.com/questions/18411494/android-listview-show-only-one-item
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMeasureSpecCustom = View.MeasureSpec.makeMeasureSpec(
            Int.MAX_VALUE shr 2, View.MeasureSpec.AT_MOST
        )
        super.onMeasure(widthMeasureSpec, heightMeasureSpecCustom)
        val params = layoutParams
        params.height = measuredHeight
    }

}