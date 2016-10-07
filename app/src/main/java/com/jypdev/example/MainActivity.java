package com.jypdev.example;

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
