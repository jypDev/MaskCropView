package com.jypdev.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.jypdev.maskcroplibrary.ImageUtil;
import com.jypdev.maskcroplibrary.MaskCropView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    MaskCropView view;

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

            }break;
        }
    }




        @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       super.onActivityResult(requestCode,resultCode,data);
            ImageUtil.onActivityResult(this,view,requestCode,resultCode,data);
    }

}
