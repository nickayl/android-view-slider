package org.javando.android.sliderview

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.children
import kotlin.math.abs

class SliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val defaultOnTouchDownAnim = AnimatorInflater.loadAnimator(context, R.animator.scaleup_animator).setDuration(100)
    private val defaultOnTouchUpAnim = AnimatorInflater.loadAnimator(context, R.animator.scaledown_animator).setDuration(100)

    var onTouchDownAnim: Animator? = defaultOnTouchDownAnim
    var onTouchUpAnim: Animator? = defaultOnTouchUpAnim

    var onItemSelectedListener: ((view: View, item: Any, index: Int) -> Unit)? = null
    var onItemClickListener: ((view: View, item: Any, index: Int) -> Unit)? = null

    var layoutManager:LayoutManager = CenteredItemLayoutManager().apply { sliderView = this@SliderView }
        set(value) {
            value.sliderView = this
            field = value
        }

    internal val centerContainer
        get() = measuredWidth / 2

    internal data class ItemViewWrapper(val position: Int, val view: View, val item: Any)

    internal val views: MutableList<ItemViewWrapper> = mutableListOf()
    var adapter: Adapter<out Any>? = null
    var selectedItemPosition = 0

    internal val nextItem: View
        get() = views[selectedItemPosition+1].view

    internal val previousItem: View
        get() = views[selectedItemPosition-1].view

    val selectedItem: View
        get() = views[selectedItemPosition].view

    fun hasRight() = selectedItemPosition < views.size-1
    fun hasLeft() = selectedItemPosition > 0

    internal fun getLeftItems(): List<View> {
        return views.subList(0, selectedItemPosition).map { it.view }
    }

    internal fun getRightItems(): List<View> {
        return if(hasRight())
            views.subList(selectedItemPosition+1, views.size).map { it.view }
        else
            emptyList()
    }

    private val listener = object : OnTouchListener {
        //private var endX = 0f
        private var startX = 0f
        private var rawStartX = 0f
        private var dxsum = 0f
        private var hasMoved = false

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            onTouchDownAnim?.setTarget(view)
            onTouchUpAnim?.setTarget(view)
            //val centerView = view.width / 2


            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dxsum = 0f
                    hasMoved = false
                    startX = event.x;
                    rawStartX = event.rawX
//                    Log.d("TEST", "Touch DOWN XY:  (x,y) = (${event.x}, ${event.y})\n"+
//                            "    Touch DOWN rawXY: (x,y) = (${event.rawX}, ${event.rawY})")
                }

                MotionEvent.ACTION_UP -> {
                    val dx = event.rawX - rawStartX;
//                    Log.d("TEST", "Touch UP XY:    (x,y) = (${event.x}, ${event.y}), DX = ($dxsum)\n" +
//                            "    Touch UP rawXY: (x,y) = (${event.rawX}, ${event.rawY}), rawDX = ($dx)")

                    val abs = abs(dx)
                    val elapsedTimeFromDownToUp = SystemClock.uptimeMillis() - event.downTime
                    if(abs <= 2 && elapsedTimeFromDownToUp <= 225) {
                        onTouchDownAnim?.cancel()
                        println("== Click detected ==")
                        view.performClick()
                        val wrapper = views.find { it.view == view }!!
                        onItemClickListener?.invoke(view, wrapper.item, wrapper.position)
                        if(abs > 0) slide(-dxsum)
                    }
                    else {
                        startOnTouchUpAnimation()
                        when {
                            abs <= (width * layoutManager.minimumScrollPercentage) -> slide(-dxsum)
                            dx < 0 -> slideRight(view, dxsum)
                            else -> slideLeft(view, dxsum)
                        }
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    startOnTouchDownAnimation()
                    val dx = event.x - startX
                    children.forEach { it.translationX += dx }
                    dxsum += dx
                    hasMoved = true
                    //Log.d("TEST", "Touch move: (x,y) = (${event.x}, ${event.y})")
                }
            }

            //view.onTouchEvent(event)
            return true
        }

        private fun startOnTouchDownAnimation() {
            if(!hasMoved)
                onTouchDownAnim?.start()
        }

        private fun startOnTouchUpAnimation() {
            if(hasMoved)
                onTouchUpAnim?.start()
        }
    }

    fun disableOnItemTouchAnimations() {
        onTouchDownAnim = null
        onTouchUpAnim = null
    }

    fun enableOnItemTouchAnimations() {
        onTouchUpAnim = defaultOnTouchUpAnim
        onTouchDownAnim = defaultOnTouchDownAnim
    }

    fun slideToPosition(position: Int) {
        while(position > selectedItemPosition && hasRight())
            slideRight(selectedItem, 0f,false)
        while(position < selectedItemPosition && hasLeft())
            slideLeft(selectedItem, 0f,false)
    }

    private fun slideRight(v: View, manualScrolledAmount: Float, animations:Boolean = true) {
        val view = views.find { v == it.view }!!
        println("Slide RIGHT detected...")
        if(hasRight()) {
            val amount = layoutManager.getSlidingAmount(view.view, SlideDirection.RIGHT) * -1
            slide(amount - manualScrolledAmount,animations)
            selectedItemPosition++
            onItemSelectedListener?.invoke(view.view, view.item, selectedItemPosition)
        } else
            slide(-manualScrolledAmount,animations)
    }

    private fun slideLeft(v: View, manualScrolledAmount: Float, animations:Boolean = true) {
        val view = views.find { v == it.view }!!
        println("Slide LEFT detected...")
        if(hasLeft()) {
            val amount = layoutManager.getSlidingAmount(view.view, SlideDirection.LEFT)
            slide(amount - manualScrolledAmount,animations)
            selectedItemPosition--
            onItemSelectedListener?.invoke(view.view, view.item, selectedItemPosition)
        } else
            slide(-manualScrolledAmount,animations)
    }

    private fun slide(pxAmount: Float, animations:Boolean = true) {
        if(animations)
            views.forEach {
                it.view.animate().setDuration(100).translationXBy(pxAmount).start()
            }
        else
            views.forEach { it.view.translationX += pxAmount }
    }

    fun initialize() {
        if(adapter != null && !adapter!!.isEmpty) {
            //layoutManager = LeftBoundLayoutManager(this)
            //layoutManager = FixedItemWidthLayoutManager(this)
            preloadViews()
        } else
            throw IllegalStateException("The adapter must be set before calling the initialize() member function")
    }

    private class ChildrenTouchInterceptor(val sliderView: SliderView, val parent: View) : OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return sliderView.listener.onTouch(parent, event)
        }
    }

    private fun preloadViews() {
        for(i in 0 until adapter!!.count) {
            val view = adapter!!.getView(i, this, null)
            views.add(ItemViewWrapper(i, view, adapter!!.getItem(i)))
            if(view is ViewGroup) {
                val interceptor = ChildrenTouchInterceptor(this, view)
                view.children.forEach { it.setOnTouchListener(interceptor) }
            }
            addView(view)
            view.setOnTouchListener(listener)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        layoutManager.checkConsistency()
        layoutManager.applyLayout()
        println("On measure called")
    }

    fun notifyDataSetChanged() {
        removeAllViews()
        views.clear()
        preloadViews()
    }

    enum class SlideDirection {
        LEFT, RIGHT
    }

    abstract class LayoutManager {
        open lateinit var sliderView: SliderView
            internal set
        internal open fun calculateEqualSpacing(width: Int) = with(sliderView) {
            ((measuredWidth - width) / 2)
        }
        abstract var itemsLeftMargin: Float
        open var minimumScrollPercentage: Float = 0.25f
            set(value) {
                if(value < 0.1 || value >= 0.9)
                    throw IllegalArgumentException("The scroll percentage must be a value between 0.1(inclusive) and 0.9(exclusive)")
                else
                    field = value
            }

        abstract fun applyLayout()
        abstract fun getSlidingAmount(view: View, direction: SlideDirection = SlideDirection.RIGHT): Float
        override fun toString(): String {
            return "LayoutManager(itemsLeftMargin=$itemsLeftMargin, minimumScrollPercentage=$minimumScrollPercentage)"
        }

        private var consistency = false
        internal open fun checkConsistency() {
            if(consistency) return
            val params = sliderView.views.first().view.layoutParams
            sliderView.views.forEach {
                if(it.view.layoutParams.width != params.width)
                    throw IllegalStateException("The views must have the same width to be consistent with this layout (${this.javaClass})")
            }
            consistency = true
        }
    }

    interface Adapter<T> {
        val count: Int
        val isEmpty: Boolean
            get() = count == 0
        fun getItem(position: Int): T
        fun getView(position: Int, parent: SliderView, convertView: View?) : View
    }

}






//    class SliderViewAdapter(val items: List<View>) {
//
//        var selectedItemPosition: Int = pivot
//        val itemSlidingWindow = mutableMapOf<Int,Int>()
////            set(value) {
////                val tmp = items[0]
////                items[0] = items[value]
////                items[value] = tmp
////                field = value
////            }
//        val selectedItem: View
//            get() = items[selectedItemPosition]
//
//        private val pivot: Int
//            get() = items.size / 2
//
//        fun hasRight() = selectedItemPosition < items.size-1
//        fun hasLeft() = selectedItemPosition > 0
//
//        internal fun indexOf(view: View) = items.indexOf(view)
//
//        internal fun getLeftItems(): List<View> {
//            return items.subList(0, selectedItemPosition)
//        }
//
//        internal fun getRightItems(): List<View> {
//            return if(hasRight())
//                items.subList(selectedItemPosition+1, items.size)
//            else
//                emptyList()
//        }
//
//        internal fun calculateItemsSlidingWindow(sliderWidth: Int, itemsLeftMargin: Int) {
//            items.forEachIndexed { index, view ->
//                val window = ((view.measuredWidth+itemsLeftMargin) * (index+1).toFloat() ) / sliderWidth.toFloat()
//                itemSlidingWindow[index] = window.toInt()
//            }
//            println()
//        }
//
//        internal fun notifyRightSlide() {
//            selectedItemPosition++
//            println("==ADAPTER== Notified RIGHT slide occurred. Selected item index is now= $selectedItemPosition")
//        }
//
//        internal fun notifyLeftSlide() {
//            selectedItemPosition--
//            println("==ADAPTER== Notified LEFT slide occurred. Selected item index is now = $selectedItemPosition")
//        }
//
//        fun nextElementForDirection(view: View, direction: SlideDirection): View? {
//            return when (direction) {
//                SlideDirection.LEFT -> {
//                    val nextElementIndex = indexOf(view) + 1
//                    if(nextElementIndex < items.size)
//                        items[nextElementIndex]
//                    null
//                }
//                SlideDirection.RIGHT -> {
//                    val previousElementIndex = indexOf(view) - 1
//                    if(previousElementIndex >= 0)
//                        items[previousElementIndex]
//                    null
//                }
//            }
//        }
//    }

//    @FunctionalInterface
//    interface OnItemSelectedListener {
//        fun onItemSelected(view: View, index: Int)
//    }
