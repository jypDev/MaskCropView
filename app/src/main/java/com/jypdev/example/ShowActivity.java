package com.jypdev.example;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by jyp on 2016. 10. 7..
 */

public class ShowActivity extends Activity {

    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_layout);
        ImageView imageView = (ImageView) findViewById(R.id.imageview);

        Intent intent = getIntent();
        if (intent != null) {
            String path = intent.getStringExtra("imagePath");
            if (path != null && path.length() > 0) {
                if (new File(path).exists()) {
                    bitmap = BitmapFactory.decodeFile(path);
                    imageView.setImageBitmap(bitmap);
                }
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bitmap != null){
            bitmap.recycle();
            bitmap = null;
        }
    }
}
