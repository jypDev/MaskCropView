package com.jypdev.maskcroplibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by JY-park on 16. 6. 17..
 */
public class ImageUtil {

    private static void init(Context context){
//        Constants.setTempPath("/data/data/"+context.getPackageName()+"/temp");
    }

    public static void getCameraImage(Context context){
        init(context);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri(Constants.TEMP_PATH+"/"+Constants.TEMP_FILENAME));
        ((Activity)context).startActivityForResult(intent, Constants.ACTIVITY_RESULT_CODE_CAMERA);
    }

    public static void getAlbumImage(Context context){
        init(context);
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri(Constants.TEMP_PATH+"/"+Constants.TEMP_FILENAME));
        ((Activity)context).startActivityForResult(intent, Constants.ACTIVITY_RESULT_CODE_ALBUM);
    }


    public static void onActivityResult(Context context, MaskCropView view, int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && view !=null) {
            switch (requestCode) {
                case Constants.ACTIVITY_RESULT_CODE_CAMERA: {
                    Bitmap bitmap = decodeSampledBitmapFromPath(context, Constants.TEMP_PATH+"/"+Constants.TEMP_FILENAME);
                    view.setOriginalBitmap(bitmap);
                    bitmap.recycle();

                }
                break;
                case Constants.ACTIVITY_RESULT_CODE_ALBUM: {
                    Uri imageUri = data.getData();
                    String imagePath = getPath(context, imageUri);


                    if (TextUtils.isEmpty(imagePath)) {
                        // 내부에 존재하지 않는 이미지인 경우 (예, Picasa)
                        File file = new File(Constants.TEMP_PATH + "/" + Constants.TEMP_FILENAME);
                        imagePath = file.getAbsolutePath();

                        try {
                            // 외부 이미지, 단말기에 저장하기
                            Bitmap bmp = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                            FileOutputStream fileStream = new FileOutputStream(file);
                            bmp.compress(Bitmap.CompressFormat.PNG, 100, fileStream);
                            fileStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            imagePath = null;
                        }
                    }

                    Bitmap bitmap = decodeSampledBitmapFromPath(context,imagePath);
                    view.setOriginalBitmap(bitmap);
                }
                break;
            }
        }else{
           //TODO
        }
    }

    public static String getPath(Context context, Uri uri) {
        String filePath = "";
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        filePath = cursor.getString(column_index);
        cursor.close();
        return filePath;
    }

    public static Uri getTempUri(String filePath) {
        return Uri.fromFile(getTempFile(filePath));
    }

    public static File getTempFile(String path) {
        File temp = new File(path);
        try {
            if (temp.exists()) {
                temp.delete();
            }else{
                temp.mkdirs();
            }

            temp.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return temp;
    }

    public static Bitmap decodeSampledBitmapFromPath(Context context, String path) {

        Bitmap bmp = null;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);

        int height = options.outHeight;
        int width = options.outWidth;
        Log.v("jyp", "");

        options.inSampleSize = calculateInSampleSize(options, getScreenWidth(context), getScreenHeight(context));

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bmp = BitmapFactory.decodeFile(path, options);

        if (height < width) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap bitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            bmp.recycle();
            return bitmap;
        }

        return bmp;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    public static int getScreenHeight(Context context) {
        Display display = ((WindowManager) context.getSystemService(context.WINDOW_SERVICE))
                .getDefaultDisplay();

        int height = 0;

        if (android.os.Build.VERSION.SDK_INT >= 13) {
            Point size = new Point();
            display.getSize(size);
            height = size.y;

        } else {
            height = display.getHeight();

        }

        return height;
    }

    public static int getScreenWidth(Context context) {
        Display display = ((WindowManager) context.getSystemService(context.WINDOW_SERVICE))
                .getDefaultDisplay();

        int width = 0;

        if (android.os.Build.VERSION.SDK_INT >= 13) {
            Point size = new Point();
            display.getSize(size);

            width = size.x;
        } else {

            width = display.getWidth();
        }

        return width;
    }
}
