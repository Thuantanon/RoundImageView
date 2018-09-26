# RoundImageView
a ImageView display round image
> 这是很久以前项目里的代码了，整理下以后方便拿来用。
  
## 效果图

<center>
<img src="https://github.com/Thuantanon/RoundImageView/blob/master/simple/simple.jpg" width="50%" height="50%" />
</center>

## 集成方式
> Step 1. Add it in your root build.gradle at the end of repositories:
```Java
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
> Step 2. Add the dependency:
```Java
dependencies {
	 implementation 'com.github.Thuantanon:RoundImageView:1.0'
}
```

## 使用方式
> XML

```Java
<com.cxh.roundimageviewlib.RoundImageView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:src="@drawable/xxx"
        app:cc_border_overlay=""
        app:cc_border_width=""
        app:cc_radius="" 
        app:cc_cover_color=""
        app:cc_border_color=""
        />
```

