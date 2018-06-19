package com.betelgeuse.calculator.ui.widget.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.PageTransformer
import com.betelgeuse.calculator.R

class CalculatorPadViewPager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewPager(context, attrs) {

    private val mStaticPagerAdapter = object : PagerAdapter() {
        override fun getCount(): Int {
            return childCount
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            return getChildAt(position)
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            removeViewAt(position)
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun getPageWidth(position: Int): Float {
            return if (position == 1) 7.0f / 9.0f else 1.0f
        }
    }

    private val mOnPageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
        private fun recursivelySetEnabled(view: View, enabled: Boolean) {
            if (view is ViewGroup) {
                for (childIndex in 0 until view.childCount) {
                    recursivelySetEnabled(view.getChildAt(childIndex), enabled)
                }
            } else {
                view.isEnabled = enabled
            }
        }

        override fun onPageSelected(position: Int) {
            if (adapter === mStaticPagerAdapter) {
                for (childIndex in 0 until childCount) {
                    // Only enable subviews of the current page.
                    recursivelySetEnabled(getChildAt(childIndex), childIndex == position)
                }
            }
        }
    }

    private val mPageTransformer = PageTransformer { view, position ->
        if (position < 0.0f) {
            // Pin the left page to the left side.
            view.translationX = width * -position
            view.alpha = Math.max(1.0f + position, 0.0f)
        } else {
            // Use the default slide transition when moving to the next page.
            view.translationX = 0.0f
            view.alpha = 1.0f
        }
    }

    init {

        adapter = mStaticPagerAdapter
        setBackgroundColor(resources.getColor(android.R.color.black))
        addOnPageChangeListener(mOnPageChangeListener)
        pageMargin = resources.getDimensionPixelSize(R.dimen.pad_page_margin)
        setPageTransformer(false, mPageTransformer)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        // Invalidate the adapter's data set since children may have been added during inflation.
        if (adapter === mStaticPagerAdapter) {
            mStaticPagerAdapter.notifyDataSetChanged()
        }
    }
}