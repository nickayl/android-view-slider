package com.domenicoaiello.testactivity

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.domenicoaiello.viewslider.LinearLayoutManager
import com.domenicoaiello.viewslider.SliderView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val res = mutableListOf(R.drawable.book1, R.drawable.book2,R.drawable.book3,R.drawable.book1,R.drawable.book2,R.drawable.book3)
        val resStrings = mutableListOf("Ciao ciao", "Che pollo", "che sei!!!", "eoeoeoeoe", "ooooooooo", "aaaaaaaaa")

        val viewSlider = findViewById<SliderView>(R.id.slider)

        viewSlider.selectedItemPosition = 3
        viewSlider.layoutManager = LinearLayoutManager()
        viewSlider.adapter = object : SliderView.Adapter<Int> {
            override val count: Int
                get() = res.size

            override fun getItem(position: Int): Int {
                return res[position]
            }

            override fun getView(position: Int, parent: SliderView, convertView: View?): View {
                var view = convertView
                if(convertView == null) {
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
                    view = ImageView(this@MainActivity).apply {
                        if(position==3)
                            layoutParams = FrameLayout.LayoutParams(850, ViewGroup.LayoutParams.WRAP_CONTENT)
                        else
                            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        setImageResource(getItem(position))
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        background = ColorDrawable(ResourcesCompat.getColor(this@MainActivity.resources, R.color.design_default_color_error, null))
                    }
                }
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
        viewSlider.initialize()



//        viewSlider.onTouchUpAnimators = AnimatorSet().apply { play(scaleUpAnim) }
//        viewSlider.onTouchDownAnimators = AnimatorSet().apply { play(scaleDownAnim) }

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
    }
}