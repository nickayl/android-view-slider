package org.javando.android.sliderview.test

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.javando.android.sliderview.CenteredItemLayoutManager
import org.javando.android.sliderview.LinearLayoutManager
import org.javando.android.sliderview.R
import org.javando.android.sliderview.SliderView

internal class MainActivity : AppCompatActivity() {

    @SuppressLint("ObjectAnimatorBinding")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_scrollview)

        val books = mutableListOf(
            R.drawable.book1,
            R.drawable.book2,
            R.drawable.book3,
            R.drawable.book1,
            R.drawable.book2,
            R.drawable.book3
        )

        val series = mutableListOf(
            R.drawable.series1,
            R.drawable.series2,
            R.drawable.series3,
            R.drawable.series1,
            R.drawable.series2,
            R.drawable.series3
        )

        var res: MutableList<Int> = series

        val viewSlider = findViewById<SliderView>(R.id.slider)

        viewSlider.selectedItemPosition = 0

        val centeredItemLayoutManager = CenteredItemLayoutManager()
        viewSlider.layoutManager = centeredItemLayoutManager
        centeredItemLayoutManager.itemsOverflow = 50
        res = books
//        val linearLayoutManager = LinearLayoutManager()
//        linearLayoutManager.itemsLeftMargin = 70f
//        viewSlider.layoutManager = linearLayoutManager

        //viewSlider.layoutManager.minimumScrollPercentage = 0.20f

        viewSlider.adapter = object : SliderView.Adapter<Int> {
            override val count: Int
                get() = res.size

            override fun getItem(position: Int): Int {
                return res[position]
            }

            override fun getView(position: Int, parent: SliderView, convertView: View?): View {
                var view = convertView
                if(convertView == null) {
                    println("Convertview $position is null. creating a new view object")
                    view = ImageView(this@MainActivity).apply {
                        if(position==3)
                            layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                        else
                            layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                        setImageResource(getItem(position))
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        //background = ColorDrawable(ResourcesCompat.getColor(this@MainActivity.resources, R.color.design_default_color_error, null))
                    }
                } else
                    println("convertView $position is NOT null, reusing previous view object...")
                return view!!
            }
        }


        //viewSlider.layoutManager = FixedItemWidthLayoutManager()
        viewSlider.onItemSelectedListener = { view, item, index ->
            println("Selected $view at index $index with item $item")
        }
        viewSlider.onItemClickListener = { view, item, index ->
            println("Clidked $view at index $index with item $item")
        }
        //viewSlider.disableOnItemTouchAnimations()
        viewSlider.onStartDragAnimation = ObjectAnimator.ofFloat(null, "translationY", -100f).apply { duration = 150 }
        viewSlider.onEndDragAnimation = ObjectAnimator.ofFloat(null, "translationY", 0f).apply { duration = 150 }
        viewSlider.initialize()


//        GlobalScope.launch {
//            delay(1500)
//            for(i in 0 until res.size-3)
//                res.removeAt(i)
//            runOnUiThread { viewSlider.notifyDataSetChanged() }
//        }

        //        val scaleAnim = ValueAnimator.ofFloat(1f, 1.2f).apply {
//            duration = 100
//            startDelay = 0
//            repeatCount = 0
//            this.addUpdateListener {
//                val v = animatedValue as Float
//                img1.scaleX = v
//                img1.scaleY = v
//            }
//        }

//        val upX = PropertyValuesHolder.ofFloat(SCALE_X, 1f, 1.2f)
//        val upY = PropertyValuesHolder.ofFloat(SCALE_Y, 1f, 1.2f)
//
//        val downX = PropertyValuesHolder.ofFloat(SCALE_X, 1.2f, 1f)
//        val downY = PropertyValuesHolder.ofFloat(SCALE_Y, 1.2f, 1f)
//
//        val scaleUpAnim = ObjectAnimator.ofPropertyValuesHolder(img1, upX, upY)
//        val scaleDownAnim = ObjectAnimator.ofPropertyValuesHolder(img1, downX, downY)
//
//        val views = listOf(
//            ImageView(this).apply {
//                layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
//                setImageResource(R.drawable.book1)
//            },
//            ImageView(this).apply {
//                layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
//                setImageResource(R.drawable.book2)
//            },
//            ImageView(this).apply {
//                layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
//                setImageResource(R.drawable.book3)
//            }, ImageView(this).apply {
//                layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
//                setImageResource(R.drawable.book1)
//            },
//            ImageView(this).apply {
//                layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
//                setImageResource(R.drawable.book2)
//            },
//            ImageView(this).apply {
//                layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
//                setImageResource(R.drawable.book3)
//            })
//
  //      val resStrings = mutableListOf("Ciao ciao", "xxxxxxxxxxxx, "yyyyyyyyyyyy", "eoeoeoeoe", "ooooooooo", "aaaaaaaaa")


        //                    view = LinearLayout(this@TestActivity).apply {
//                        orientation = LinearLayout.VERTICAL
//                        layoutParams = FrameLayout.LayoutParams(300, WRAP_CONTENT).apply { gravity = Gravity.CENTER }
//                        background = ColorDrawable(ResourcesCompat.getColor(this@TestActivity.resources, R.color.design_default_color_error, null))
//                        val tv1 = TextView(this@TestActivity).apply {
//                            text = resStrings[position];
//                            background = ColorDrawable(ResourcesCompat.getColor(this@TestActivity.resources, R.color.design_default_color_primary, null))
//                            setOnClickListener {
//                                println("$text Clicked222!!!!")
//                            }
//                        }
//                        val tv2 = TextView(this@TestActivity).apply {
//                            text = resStrings[position];
//                            background = ColorDrawable(ResourcesCompat.getColor(this@TestActivity.resources, R.color.design_default_color_secondary, null))
//                            setOnClickListener {
//                                println("$text Clicked1111!!!!")
//                            }
//                        }
//                        addView(tv1)
//                        addView(tv2)
//                    }
    }
}