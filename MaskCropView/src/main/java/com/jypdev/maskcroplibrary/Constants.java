package com.jypdev.maskcroplibrary;

import java.io.File;

/**
 * Created by JY-park on 16. 6. 17..
 */
public class Constants {
    public static final int ACTIVITY_RESULT_CODE_CAMERA = 100;
    public static final int ACTIVITY_RESULT_CODE_ALBUM = 200;

    public static String TEMP_PATH = "/sdcard";
    public static String TEMP_FILENAME = "temp";

    public static void setTempPath(String tempPath) {
        File file = new File(tempPath);
        if(file.isDirectory() && !file.exists()){
            file.mkdirs();
        }
        TEMP_PATH = tempPath;
    }

    public static void setTempFilename(String tempFilename) {
        TEMP_FILENAME = tempFilename;
    }
}
