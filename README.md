
### MaskCropView

> Android MaskCropView!

이미지의 원하는 부분을 직접 자를 수 있다.

### Sample

![img](https://github.com/jypDev/maskcropview/blob/master/sample/Screenshot_20161007-152518.png)
![img](https://github.com/jypDev/maskcropview/blob/master/sample/Screenshot_20161007-152942.png)
![img](https://github.com/jypDev/maskcropview/blob/master/sample/Screenshot_20161007-152950.png)

### Usage

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.jypdev.maskcroplibrary.MaskCropView
        android:id="@+id/maskview"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <Button
        android:id="@+id/camera_button"
        android:layout_width="100dp"
        android:layout_height="50dp"
        android:layout_alignParentBottom="true"
        android:text="camera"/>

    <Button
        android:id="@+id/gallery_button"
        android:layout_width="100dp"
        android:layout_height="50dp"
        android:layout_alignParentBottom="true"
        android:layout_toRightOf="@+id/camera_button"
        android:text="gallery"/>

    <Button
        android:id="@+id/confirm_button"
        android:layout_width="100dp"
        android:layout_height="50dp"
        android:layout_alignParentBottom="true"
        android:layout_toRightOf="@+id/gallery_button"
        android:text="confirm"/>


</RelativeLayout>


```

```java
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.jypdev.maskcroplibrary.ImageUtil;
import com.jypdev.maskcroplibrary.MaskCropView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private MaskCropView view;
    private String imagePath ="/sdcard/capture.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.camera_button).setOnClickListener(this);
        findViewById(R.id.gallery_button).setOnClickListener(this);
        findViewById(R.id.confirm_button).setOnClickListener(this);
        view = (MaskCropView) findViewById(R.id.maskview);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera_button: {
                ImageUtil.getCameraImage(this);
            }
            break;
            case R.id.gallery_button: {
                ImageUtil.getAlbumImage(this);
            }
            break;
            case R.id.confirm_button:{
                Bitmap bitmap = view.getPicture();
                if(bitmap!=null) {
                    FileOutputStream fos;
                    try {
                        fos = new FileOutputStream(imagePath);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this,ShowActivity.class);
                        intent.putExtra("imagePath",imagePath);
                        startActivity(intent);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "null", Toast.LENGTH_SHORT).show();
                }
            }break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       super.onActivityResult(requestCode,resultCode,data);
            ImageUtil.onActivityResult(this,view,requestCode,resultCode,data);
    }

}

```

### Setup

1.add this in your `build.gradle` file in root project

```
allprojects {
    repositories {
        ...
        maven { url "https://www.jitpack.io" }
    }
}
```

2.add the following dependency

```
dependencies {
    compile 'com.github.jypdev:maskcropview:1.1.1'
}
```

### License
```
The MIT License (MIT)

Copyright (c) 2016 jypDev

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
