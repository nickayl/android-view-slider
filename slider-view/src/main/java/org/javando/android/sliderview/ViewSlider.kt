package org.javando.android.sliderview

import android.animation.Animator
import android.animation.AnimatorInflater
import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ScrollView
import androidx.core.view.children
import kotlin.math.abs

class SliderView @JvmOverloads constructor(context: Context,
                                           attrs: AttributeSet? = null,
                                           defStyleAttr: Int = 0,
                                           defStyleRes: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val logger = Logger.instance
    private var scrollView: ScrollView? = null
    private val defaultOnTouchDownAnim = AnimatorInflater.loadAnimator(context, R.animator.scaleup_animator)
        .setDuration(100)
    private val defaultOnTouchUpAnim = AnimatorInflater.loadAnimator(context, R.animator.scaledown_animator)
        .setDuration(100)

    var onStartDragAnimation: Animator? = defaultOnTouchDownAnim
    var onEndDragAnimation: Animator? = defaultOnTouchUpAnim

    var onItemSelectedListener: ((view: View, item: Any, index: Int) -> Unit)? = null
    var onItemClickListener: ((view: View, item: Any, index: Int) -> Unit)? = null

    var layoutManager: LayoutManager = CenteredItemLayoutManager().apply { sliderView = this@SliderView }
        set(value) {
            value.sliderView = this
            field = value
        }

    internal val centerContainer
        get() = measuredWidth / 2

    internal data class ItemViewWrapper(val position: Int, val view: View, val item: Any)

    internal val views: MutableList<ItemViewWrapper> = mutableListOf()
    lateinit var adapter: Adapter<out Any>
    var selectedItemPosition = 0

    private fun checkSelectedItemPosition() {
        if (selectedItemPosition >= views.size) throw IllegalArgumentException("The selected item position is invalid")
    }

    internal val nextItem: View
        get() = views[selectedItemPosition + 1].view

    internal val previousItem: View
        get() = views[selectedItemPosition - 1].view

    val selectedItem: View
        get() = views[selectedItemPosition].view

    fun hasRight() = selectedItemPosition < views.size - 1
    fun hasLeft() = selectedItemPosition > 0

    internal fun getLeftItems(): List<View> =
            views.subList(0, selectedItemPosition).map { it.view }

    internal fun getRightItems(): List<View> =
        if (hasRight())
            views.subList(selectedItemPosition + 1, views.size).map { it.view }
        else emptyList()


    private val listener = object : OnTouchListener {
        private var unsignedDxsum = 0f
        var startX = 0f
        var rawStartX = 0f
        var startY = 0f
        var dxsum: Float = 0f
        var animationsEnabled = false
        //private var hasMoved = false

        private var onCancelDispatched = false
        var interruptedTouchView: View? = null

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            logger.log("scroll ${event.action}")

            when (event.action) {
                ACTION_DOWN -> {
                    onStartDragAnimation?.setTarget(view)
                    onEndDragAnimation?.setTarget(view)
                    onCancelDispatched = false
                    animationsEnabled = false
                    dxsum = 0f
                   // hasMoved = false
                    startX = event.x;
                    startY = event.y;
                    rawStartX = event.rawX
                    unsignedDxsum = 0f
                }

                ACTION_UP -> {
                    val dx = event.rawX - rawStartX;
                    val abs = abs(dx)
                    val elapsedTimeFromDownToUp = SystemClock.uptimeMillis() - event.downTime
                    if (abs <= 2 && elapsedTimeFromDownToUp <= 225) {
                        onStartDragAnimation?.cancel()
                        logger.log("Click detected")
                       // view.performClick()
                        val wrapper = views.find { it.view == view }!!
                        onItemClickListener?.invoke(view, wrapper.item, wrapper.position)
                        if (abs > 0) slide(-dxsum)
                    } else {
                        startOnTouchUpAnimation()
                        when {
                            abs <= (width * layoutManager.minimumScrollPercentage) -> slide(-dxsum)
                            dx < 0 -> slideRight(view, dxsum)
                            else -> slideLeft(view, dxsum)
                        }
                    }
                    interruptedTouchView = null
                    logger.log("Touch UP: (startX, dx, x,y) = ($startX, $dx, ${event.x}, ${event.y})")
                }

                ACTION_MOVE -> {
                    val dx = event.x - startX
                    val dy = event.y - startY

                    if(!onCancelDispatched && unsignedDxsum >= 3f) {
                        dispatchActionCancelEvent(view, event)
                        animationsEnabled = true
                        startOnTouchDownAnimation()
                    }

                    children.forEach { it.translationX += dx }
                    dxsum += dx
                    unsignedDxsum += abs(dx)
                  //  hasMoved = true

                  Log.d("TEST", "[LISTENER]Touch MOVE: dy=$dy, eventY=${event.y} startY=$startY (startX, dx, udx, x,y) = ($startX, $dx, $unsignedDxsum, ${event.x}, ${event.y})")
                    return true
                }

                ACTION_CANCEL -> {
                    interruptedTouchView = view
                    //onCancelDispatched = true
                }
            }

            return false
        }

        private fun dispatchActionCancelEvent(view: View, e: MotionEvent) {
            logger.log("Sending ACTION_CANCEL to $view")
            val cancelEvent = MotionEvent.obtain(e.downTime, e.eventTime, ACTION_CANCEL, e.x, e.y, e.metaState)
            view.onTouchEvent(cancelEvent)
            if(view is ViewGroup) {
                view.children.forEach {
                    if (it.hasOnClickListeners()) {
                        it.onTouchEvent(cancelEvent)
                        logger.log("Sending ACTION_CANCEL to children $view")
                    }
                }
            }
            onCancelDispatched = true
        }

        fun startOnTouchDownAnimation() {
            if (animationsEnabled) onStartDragAnimation?.start()
        }

        fun startOnTouchUpAnimation() {
            if (animationsEnabled) onEndDragAnimation?.start()
        }
    }

    fun disableOnItemTouchAnimations() {
        onStartDragAnimation = null
        onEndDragAnimation = null
    }

    fun enableOnItemTouchAnimations() {
        onEndDragAnimation = defaultOnTouchUpAnim
        onStartDragAnimation = defaultOnTouchDownAnim
    }

    fun slideToPosition(position: Int) {
        while (position > selectedItemPosition && hasRight()) slideRight(selectedItem, 0f, false)
        while (position < selectedItemPosition && hasLeft()) slideLeft(selectedItem, 0f, false)
    }

    private fun slideRight(v: View, manualScrolledAmount: Float, animations: Boolean = true) {
        val view = views.find { v == it.view }!!
        logger.log("Slide RIGHT detected ")
        if (hasRight()) {
            val amount = layoutManager.getSlidingAmount(view.view, SlideDirection.RIGHT) * -1
            slide(amount - manualScrolledAmount, animations)
            selectedItemPosition++
            onItemSelectedListener?.invoke(view.view, view.item, selectedItemPosition)
        } else slide(-manualScrolledAmount, animations)
    }

    private fun slideLeft(v: View, manualScrolledAmount: Float, animations: Boolean = true) {
        val view = views.find { v == it.view }!!
        logger.log("Slide LEFT detected")
        if (hasLeft()) {
            val amount = layoutManager.getSlidingAmount(view.view, SlideDirection.LEFT)
            slide(amount - manualScrolledAmount, animations)
            selectedItemPosition--
            onItemSelectedListener?.invoke(view.view, view.item, selectedItemPosition)
        } else slide(-manualScrolledAmount, animations)
    }

    private fun slide(pxAmount: Float, animations: Boolean = true) {
        if (animations) views.forEach {
            it.view.animate().setDuration(100).translationXBy(pxAmount).start()
        }
        else views.forEach { it.view.translationX += pxAmount }
    }

    fun initialize() {
        if (this::adapter.isInitialized && !adapter.isEmpty) {
            preloadViews()
            preventScrollViewTouchInterceptor()
        } else throw IllegalStateException("The adapter must be set before calling the initialize() member function")
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun preventScrollViewTouchInterceptor() {
        var parent = this.parent
        while (parent != null) {
            if (parent is ScrollView) {
                parent.setOnTouchListener(object : OnTouchListener {

                    //private var hasMoved: Boolean = false
                    private var controlPassedToScrollView = false
                    private var startX = 0f
                    private var rawStartX = 0f
                    var dxsum: Float = 0f

                    override fun onTouch(v: View, event: MotionEvent): Boolean {

                        if (listener.interruptedTouchView == null)
                            return false

                        var what = "UMKNOWN"

                        when (event.action) {
                            ACTION_UP -> {
                                what = "UP"
                                if (dxsum == 0f) dxsum = listener.dxsum
                                val view = listener.interruptedTouchView!!
                                val abs = abs(dxsum)
                                val elapsedTimeFromDownToUp = SystemClock.uptimeMillis() - event.downTime
                                if (abs <= 2 && elapsedTimeFromDownToUp <= 225) {
                                    onStartDragAnimation?.cancel()
                                    logger.log("== Click detected ==")
                                    val wrapper = views.find { it.view == view }!!
                                    onItemClickListener?.invoke(view,
                                                                wrapper.item,
                                                                wrapper.position)
                                    if (abs > 0) slide(-dxsum)
                                } else {
                                    listener.startOnTouchUpAnimation()
                                    when {
                                        abs <= (width * layoutManager.minimumScrollPercentage) -> slide(-dxsum)
                                        dxsum < 0 -> slideRight(view, dxsum)
                                        else -> slideLeft(view, dxsum)
                                    }
                                }
                                startX = 0f
                                dxsum = 0f
                                rawStartX = 0f
                                listener.interruptedTouchView = null
                                controlPassedToScrollView = false
                                logger.log("Touch $what coming from ScrollView ${event.action} (x,y)=(${event.x}, ${event.y} dxsum=$dxsum, startX=$startX.  $event)")
                                return true
                            }
                            ACTION_MOVE -> {
                                if(controlPassedToScrollView)
                                    return false
                                else if (abs(listener.dxsum) <= 5) {
                                    logger.log("passing control to scrollview... ${listener.dxsum}")
                                    children.forEach { it.translationX += -listener.dxsum }
                                    listener.dxsum = 0f
                                    listener.animationsEnabled = false
                                    controlPassedToScrollView = true
                                    return false
                                }

                                //hasMoved = true
                                if (dxsum == 0f) {
                                    listener.animationsEnabled = true
                                    dxsum = listener.dxsum
                                }

                                if (rawStartX == 0f) rawStartX = event.rawX
                                if (startX == 0f) startX = event.x
                                what = "MOVE"
                                val dx = event.x - startX

                                dxsum += dx
                                startX = event.x
                                children.forEach { it.translationX += dx; }
                            }
                        }

                        logger.log("Touch $what coming from ScrollView ${event.action} (x,y)=(${event.x}, ${event.y} dxsum=$dxsum, startX=$startX.  $event)")
                        return true
                    }

                })
                logger.log("Detected a parent ScrollView")
                scrollView = parent
            }
            parent = parent.parent
        }

    }

    private class ChildrenTouchInterceptor(val sliderView: SliderView, val parent: View) : OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return sliderView.listener.onTouch(parent, event)
        }
    }

    private fun preloadViews() {

        for (i in 0 until adapter.count) {
            val convertView = if (views.size > i) views[i] else null
            val view = adapter.getView(i, this, convertView?.view)
            if (convertView == null) {
                views.add(ItemViewWrapper(i, view, adapter.getItem(i)))
                if (view is ViewGroup) {
                    val interceptor = ChildrenTouchInterceptor(this, view)
                    view.children.forEach { it.setOnTouchListener(interceptor) }
                }
                view.isClickable = true
                view.isFocusable = true
                addView(view, i)
                view.setOnTouchListener(listener)
            }
        }
        views.filterIndexed { index, _ -> index >= adapter.count }.apply {
            forEach { removeView(it.view) }
            views.removeAll(this)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        layoutManager.checkConsistency()
        layoutManager.applyLayout()
        logger.log("On measure called")
    }

    fun notifyDataSetChanged() {
        preloadViews()
        checkSelectedItemPosition()
        layoutManager.applyLayout()
    }

    enum class SlideDirection {
        LEFT, RIGHT
    }

    abstract class LayoutManager {
        internal open lateinit var sliderView: SliderView
            internal set

        abstract var itemsLeftMargin: Float
        open var minimumScrollPercentage: Float = 0.25f
            set(value) {
                if (value < 0.1 || value >= 0.9) throw IllegalArgumentException("The scroll percentage must be a value between 0.1(inclusive) and 0.9(exclusive)")
                else field = value
            }

        protected open fun calculateEqualSpacing(width: Int) = with(sliderView) {
            ((measuredWidth - width) / 2)
        }
        internal abstract fun applyLayout()
        internal abstract fun getSlidingAmount(view: View, direction: SlideDirection = SlideDirection.RIGHT): Float

        private var consistency = false
        internal open fun checkConsistency() {
            if (consistency) return
            val params = sliderView.views.first().view.layoutParams
            sliderView.views.forEach {
                if (it.view.layoutParams.width != params.width) throw IllegalStateException("The views must have the same width to be consistent with this layout (${this.javaClass})")
            }
            consistency = true
        }

        override fun toString(): String {
            return "LayoutManager(itemsLeftMargin=$itemsLeftMargin, minimumScrollPercentage=$minimumScrollPercentage)"
        }
    }

    interface Adapter<T> {
        val count: Int
        val isEmpty: Boolean
            get() = count == 0

        fun getItem(position: Int): T
        fun getView(position: Int, parent: SliderView, convertView: View?): View
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
//            logger.log("==ADAPTER== Notified RIGHT slide occurred. Selected item index is now= $selectedItemPosition")
//        }
//
//        internal fun notifyLeftSlide() {
//            selectedItemPosition--
//            logger.log("==ADAPTER== Notified LEFT slide occurred. Selected item index is now = $selectedItemPosition")
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

//private fun dispatchTouchEvent(v: View, event: MotionEvent) {
//    if(v is ViewGroup) {
//        v.children.forEach {
//            val xy = IntArray(2)
//            it.getLocationOnScreen(xy)
//            if (((event.rawX >= xy[0]) && (event.rawX <= xy[0] + it.width)) && (event.rawY >= xy[1] && (event.rawY <= xy[1] + it.height))) {
//                //it.performClick()
//                it.onTouchEvent(event)
//                logger.log("${it.javaClass.name} eventRawX=${event.rawX} eventRawY=${event.rawY} itX=${xy[0]} itY=${xy[1]}")
//            }
//        }
//    }
//}
