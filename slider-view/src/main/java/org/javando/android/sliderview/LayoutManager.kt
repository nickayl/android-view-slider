package org.javando.android.sliderview

import android.view.View
import kotlin.math.min

class LeftBoundLayoutManager : SliderView.LayoutManager()  {

    //var rightItemMargin: Float = 100f
    override var itemsLeftMargin = 50f
    var itemsOverflow:Int = 50
    override var minimumScrollPercentage = 0.25f
    var leftBoundMargin: Float = itemsLeftMargin

    override fun applyLayout() {
        var previous = with(sliderView.selectedItem) {
            x = leftBoundMargin
            this
        }

        sliderView.getRightItems().forEachIndexed { index, view ->
            view.x = previous.x + previous.measuredWidth + (sliderView.measuredWidth - previous.measuredWidth - leftBoundMargin) - itemsOverflow
            previous = view
        }

        previous = sliderView.selectedItem

        sliderView.getLeftItems().reversed().forEachIndexed { index, view ->
            view.x = previous.x - (previous.measuredWidth + (sliderView.measuredWidth - previous.measuredWidth - leftBoundMargin) - itemsOverflow)
            previous = view
        }
    }

    override fun getSlidingAmount(view: View, direction: SliderView.SlideDirection): Float {
        return  (sliderView.measuredWidth.toFloat() - itemsOverflow - leftBoundMargin)
    }

    override fun checkConsistency() {

    }
}

class CenteredItemLayoutManager : SliderView.LayoutManager() {

    override var itemsLeftMargin: Float = 0f
    var itemsOverflow:Int = 0
    override var minimumScrollPercentage = 0.22f
    override var sliderView: SliderView
        get() = super.sliderView
        set(value) {
            itemsOverflow = value.getPxFromDp(25f)
            super.sliderView = value
        }

    private fun centerSelectedItem(): View {
        with(sliderView.selectedItem) {
            x = ((sliderView.centerContainer - (measuredWidth/2))).toFloat()
            return this
        }
    }

    override fun applyLayout() {
        var preceding: View = centerSelectedItem()

        sliderView.getLeftItems().reversed().forEach { view ->
            view.x = preceding.x - (calculateEqualSpacing(preceding.measuredWidth) + preceding.measuredWidth - itemsOverflow).toFloat()
            preceding = view
        }

        preceding = sliderView.selectedItem

        sliderView.getRightItems().forEach { view ->
            view.x = preceding.x + (calculateEqualSpacing(preceding.measuredWidth) + preceding.measuredWidth - itemsOverflow).toFloat()
            preceding = view
        }

        fun setItemOverflow(px: Int) {
            itemsOverflow = px
        }

        fun setItemOverflow(dp: Float) {
            itemsOverflow = sliderView.getPxFromDp(dp)
        }
    }

    override fun getSlidingAmount(view: View, direction: SliderView.SlideDirection): Float
            = with(sliderView) {
        (centerContainer + (view.measuredWidth/2) - itemsOverflow).toFloat()
    }
}

class LinearLayoutManager : SliderView.LayoutManager()  {

    override var itemsLeftMargin: Float = 50f
    override var minimumScrollPercentage: Float = 0.20f
    val containerViewWidth by lazy { sliderView.measuredWidth }
    private var fixedWidth: Int = 0

    override fun applyLayout() {
        //sliderView.calculateItemsSlidingWindow(sliderView.measuredWidth, itemsLeftMargin.toInt())


        val tmp = sliderView.selectedItemPosition
        sliderView.selectedItemPosition = 0

        var previous = with(sliderView.selectedItem) {
            x = itemsLeftMargin
            fixedWidth = measuredWidth
            this
        }

        if(fixedWidth <= 0)
            throw IllegalStateException("Please provide a valid view-fixed width when using the ${this.javaClass.name} LayoutManager. (must be a value > 0)")

        sliderView.getLeftItems().forEach { view ->
            view.x = previous.x - (previous.measuredWidth + (itemsLeftMargin))
            previous = view
        }

        previous = sliderView.selectedItem

        sliderView.getRightItems().forEach { view ->
            view.x = previous.x + previous.measuredWidth + (itemsLeftMargin)
            previous = view
        }

        //sliderView.selectedItemPosition = tmp
        sliderView.slideToPosition(tmp)

    }

    override fun getSlidingAmount(view: View, direction: SliderView.SlideDirection): Float =
        with(sliderView) {
            if (direction == SliderView.SlideDirection.RIGHT) {
                return if (nextItem.measuredWidth < selectedItem.measuredWidth)
                    selectedItem.measuredWidth + itemsLeftMargin
                else min(
                    nextItem.measuredWidth,
                    selectedItem.measuredWidth
                ) + itemsLeftMargin
            }
            if (previousItem.measuredWidth > selectedItem.measuredWidth)
                return previousItem.measuredWidth + itemsLeftMargin

            return min(previousItem.measuredWidth, selectedItem.measuredWidth) + itemsLeftMargin
        }


    override fun checkConsistency() {

    }
}