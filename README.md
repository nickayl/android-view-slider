# SVS - A Simple View Slider for Android

**SVS** is a simple View Slider for android, that will automatically animate the sliding of views based on a Layout Manager.
<div align="center"><img src="https://github.com/cyclonesword/android-view-slider/blob/master/readme-animgif-centered-layout.gif?raw=true"></div>

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
implementation 'com.github.cyclonesword:android-view-slider:1.0.RC'
```


## Basic Usage

### You can quick start your development using the default centered-view based Layout Manager.


Once obtained an instance of the ViewSlider from your XML layout file,  implement an adapter to provide the views to be displayed by SVS and finally call the initialize() function, like the example below:
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
// To listen for the event selection event, implement a callback function:
viewSlider.onItemSelectedListener = { view, item, index ->  
  println("Selected $view at index $index with item $item")  
} 
 
// To listen for view click event, implement a callback function:
viewSlider.onItemClickListener = { view, item, index ->  
  println("Clidked $view at index $index with item $item")  
}
```

## Customizations

The customizations depends on the Layout Manager used. The default is the `CenteredItemLayoutManager`.

### Set the selected view 
 In the default centered-view layout manager, this will start the SVS with the selected item at the center. 
 An invalid value will throw an Exception.
``` kotlin
viewSlider.selectedItemPosition = 3
```

### Set a different layout manager
There are three built-in Layout Managers: 
<b>`CenteredItemLayoutManager`</b>,
<b>`LeftBoundLayoutManager`</b> and 
<b>`LinearLayoutManager`.</b>

``` kotlin
viewSlider.layoutManager = CenteredItemLayoutManager()
```