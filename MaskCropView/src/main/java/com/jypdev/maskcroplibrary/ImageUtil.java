package com.jypdev.maskcroplibrary;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by JY-park on 16. 6. 17..
 */
public class ImageUtil {

    private static void init(Context context) {
        //시스템앱에서 개별앱 디렉토리 접근이 안되는듯
//        Constants.setTempPath("/data/data/"+context.getPackageName()+"/temp");
    }

    public static void getCameraImage(Context context) {
        init(context);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri(Constants.TEMP_PATH + "/" + Constants.TEMP_FILENAME));
        ((Activity) context).startActivityForResult(intent, Constants.ACTIVITY_RESULT_CODE_CAMERA);
    }

    public static void getAlbumImage(Context context) {
        init(context);
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri(Constants.TEMP_PATH + "/" + Constants.TEMP_FILENAME));
        ((Activity) context).startActivityForResult(intent, Constants.ACTIVITY_RESULT_CODE_ALBUM);
    }


    public static void onActivityResult(Context context, MaskCropView view, int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && view != null) {
            switch (requestCode) {
                case Constants.ACTIVITY_RESULT_CODE_CAMERA: {
                    Bitmap bitmap = decodeSampledBitmapFromPath(context, Constants.TEMP_PATH + "/" + Constants.TEMP_FILENAME, null);
                    if (bitmap != null) {
                        view.setOriginalBitmap(bitmap);
                        bitmap.recycle();
                    }
                }
                break;
                case Constants.ACTIVITY_RESULT_CODE_ALBUM: {
                    Uri imageUri = data.getData();
                    String imagePath = getPath(context, imageUri);


                    if (TextUtils.isEmpty(imagePath)) {
                        File file = new File(Constants.TEMP_PATH + "/" + Constants.TEMP_FILENAME);
                        imagePath = file.getAbsolutePath();

                        try {
                            Bitmap bmp = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                            FileOutputStream fileStream = new FileOutputStream(file);
                            bmp.compress(Bitmap.CompressFormat.PNG, 100, fileStream);
                            fileStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            imagePath = null;
                        }
                    }

                    Bitmap bitmap = decodeSampledBitmapFromPath(context, imagePath, imageUri);
                    if (bitmap != null) {
                        view.setOriginalBitmap(bitmap);
                    }
                }
                break;
                case Constants.ACTIVITY_RESULT_CODE_CROP: {
//                    Uri imageUri = data.getData();
//                    String imagePath = getPath(context, imageUri);

                    // crop된 이미지를 저장하기 위한 파일 경로
                    String filePath = Constants.TEMP_PATH + "/" + Constants.TEMP_CROP_FILENAME;

                    Bitmap bitmap = decodeSampledBitmapFromPath(context, filePath, null);
                    if (bitmap != null) {
                        view.setOriginalBitmap(bitmap);
                    }
                }
                break;
            }
        } else {
            //TODO
        }
    }

    public static String getPath(Context context, Uri uri) {
        String filePath = "";
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null || cursor.getCount() < 1) {
            return null; // no cursor or no record
        }
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
            } else {
                temp.mkdirs();
            }

            temp.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return temp;
    }

    public static String getPathFromUri(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToNext();
        String path = cursor.getString(cursor.getColumnIndex("_data"));
        cursor.close();
        return path;
    }

    public static Uri getUriFromPath(Context context, String path) {
        Uri fileUri = Uri.parse(path);
        String filePath = fileUri.getPath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, "_data = '" + filePath + "'", null, null);
        cursor.moveToNext();
        int id = cursor.getInt(cursor.getColumnIndex("_id"));
        Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        return uri;
    }

    /**
     * 파일 복사
     * @param srcFile : 복사할 File
     * @param destFile : 복사될 File
     * @return
     */
    public static boolean copyFile(File srcFile, File destFile) {
        boolean result = false;
        try {
            InputStream in = new FileInputStream(srcFile);
            try {
                result = copyToFile(in, destFile);
            } finally  {
                in.close();
            }
        } catch (IOException e) {
            result = false;
        }
        return result;
    }

    /**
     * Copy data from a source stream to destFile.
     * Return true if succeed, return false if failed.
     */
    private static boolean copyToFile(InputStream inputStream, File destFile) {
        try {
            OutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                out.close();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static Bitmap decodeSampledBitmapFromPath(Context context, String path, Uri imageUri) {

        Bitmap bmp = null;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);

        int height = options.outHeight;
        int width = options.outWidth;

        options.inSampleSize = calculateInSampleSize(options, getScreenWidth(context), getScreenHeight(context));

        options.inJustDecodeBounds = false;
        bmp = BitmapFactory.decodeFile(path, options);

        if (height < width) {
            if(imageUri!=null) {
                File originalFile = new File(getPath(context, imageUri));
                File copyImage = new File(Constants.TEMP_PATH+"/"+Constants.TEMP_CROP_FILENAME);
                copyFile(originalFile,copyImage);
                Uri saveUri = Uri.fromFile(copyImage);
                //crop
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(saveUri, "image/*");
                // Crop한 이미지를 저장할 Path
                intent.putExtra("output", saveUri);
//                intent.putExtra("output",imageUri);
                intent.putExtra("aspectX", 9); // crop 박스의 x축 비율
                intent.putExtra("aspectY", 16); // crop 박스의 y축 비율
                intent.putExtra("scale", true);
                intent.putExtra("noFaceDetection", true);
                ((Activity) context).startActivityForResult(intent, Constants.ACTIVITY_RESULT_CODE_CROP);
                return null;
            }

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
