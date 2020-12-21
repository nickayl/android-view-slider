# SVS - A Simple View Slider for Android
<br>[![](https://jitpack.io/v/cyclonesword/android-view-slider.svg)](https://jitpack.io/#cyclonesword/android-view-slider)
[![](https://badgen.net/badge/android/API%2021+/green)](https://jitpack.io/#cyclonesword/android-view-slider)
[![](https://badgen.net/badge/license/MIT/red)](https://jitpack.io/#cyclonesword/android-view-slider)
[![](https://badgen.net/badge/kotlin/1.4)](https://jitpack.io/#cyclonesword/android-view-slider)




**SVS** is a simple View Slider for android, that will automatically animate the sliding of views based on a Layout Manager.
<div style="width:100%">
<img style="float:left;" src="https://github.com/cyclonesword/android-view-slider/blob/master/readme-animgif-centered-layout.gif?raw=true">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<img style="float:right" src="https://github.com/cyclonesword/android-view-slider/blob/master/readme-animgif-linear-layout.gif?raw=true">
</div>

The left image shows the `CenteredItemLayoutManager`, the right one the `LinearLayoutManager`.

## Installation instructions

#### Android gradle
First you have to add the jitpack repository to your global build.gradle file:
``` groovy
allprojects {
        repositories {
            maven { url 'https://jitpack.io' }
        }
    }
```


Then, add the dependency to your project-local build.gradle :
``` groovy
implementation 'com.github.cyclonesword:android-view-slider:1.0'
```


## Basic Usage

### You can quick start your development using the default centered-view based Layout Manager.

Once obtained an instance of the ViewSlider from your XML layout file,  implement the SliderView.Adapter interface to provide the views to be displayed by SVS. 
At the end call the **`initialize()`** function, like the example below:
``` kotlin
val res = listOf(R.drawable.book1, R.drawable.book2,R.drawable.book3,R.drawable.book1,R.drawable.book2,R.drawable.book3)

val viewSlider = findViewById<SliderView>(R.id.slider)  
viewSlider.adapter = object : SliderView.Adapter<Int> {  
    override val count: Int  
        get() = res.size  
  
    override fun getItem(position: Int): Int {  
        return res[position]  
    }  
  
    override fun getView(position: Int, parent: SliderView, convertView: View?): View {  
        var view = convertView  
        if(convertView == null) {  
            view = ImageView(this@MainActivity).apply {  
            layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)  
            setImageResource(getItem(position))  
            scaleType = ImageView.ScaleType.CENTER_CROP  
         }  
     }  
     return view!!  
    }  
}
// It's important to call this member function, otherwise the SldierView won't work!
viewSlider.initialize()
```
### Listening for events

``` kotlin
// To listen for the selection event, implement a callback function:
viewSlider.onItemSelectedListener = { view, item, index ->  
  println("Selected $view at index $index with item $item")  
} 
 
// To listen for the view click event, implement a callback function:
viewSlider.onItemClickListener = { view, item, index ->  
  println("Clidked $view at index $index with item $item")  
}
```

## Customizations

The customizations depends on the Layout Manager used. The default is the `CenteredItemLayoutManager`.

### Set the selected view 
The default position is 0 (the first element provided by the adapter).
``` kotlin
viewSlider.selectedItemPosition = 3
```

### Set custom view animation 
You can set a custom set of animations to be performed when the user starts dragging onto the view.
Assign an `ObjectAnimator` instance to the `onStartDragAnimation` and  `onEndDragAnimation`  and leave the target parameter to `null`, becouse it will be dynamically set once a view is touched.
``` kotlin
viewSlider.onStartDragAnimation = ObjectAnimator.ofFloat(null, "translationY", -100f).apply { 
duration = 150 }  

viewSlider.onEndDragAnimation = ObjectAnimator.ofFloat(null, "translationY", 0f).apply {
duration = 150 }
```

### Disable view animations 
If you want to disable the onDragStart/End animations:
``` kotlin
viewSlider.disableOnItemTouchAnimations()
```

### Set a different layout manager
There are three built-in Layout Managers: 
<b>`CenteredItemLayoutManager`</b>,
<b>`LeftBoundLayoutManager`</b> and 
<b>`LinearLayoutManager`.</b>

``` kotlin
viewSlider.layoutManager = CenteredItemLayoutManager()
```
**All Layout Managers** support setting a custom minimum percentage of the viewport's width necessary to perform the scroll. By default it is set at 22% .

``` kotlin
viewSlider.layoutManager.minimumScrollPercentage = 0.20f
```
The **CenteredItemLayoutManager** support also the customization of the amount of overflow for the left and right views:
``` kotlin
val centeredItemLayoutManager = CenteredItemLayoutManager()  
centeredItemLayoutManager.itemsOverflow = 100
viewSlider.layoutManager = centeredItemLayoutManager  
```

The **LinearLayoutManager** has the `itemsLeftMargin`  property that is used to give a left margin to the views:
``` kotlin  
val linearLayoutManager = LinearLayoutManager() 
linearLayoutManager.itemsLeftMargin= 80f  
viewSlider.layoutManager = linearLayoutManager
```

### Dynamically modify underlying dataset
Like normal ListView (or RecyclerView) you can dynamically add, remove or change the views mapped by your adapter by simply call the `notifyDataSetChanged` function on the sliderView instance. 
Suppose you have a network operation and at the end of it you want to remove or add some views to your sliderView, then you can do like the following example:

``` kotlin
GlobalScope.launch {  // Simulating a network operation on a background thread...
  delay(1500)  // A delay to simulate network operation
  
  ... // Additional operations...
  
  for(i in 0 until res.size-3)  // suppose we want to remove all elements but the last 3
      res.removeAt(i)  //res is our sample resource list containing drawables
      
  // At the end notify the viewSlider and it will automatically update itself
  runOnUiThread { viewSlider.notifyDataSetChanged() }  
}
```
