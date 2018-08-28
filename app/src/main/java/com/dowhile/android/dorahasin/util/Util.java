package com.dowhile.android.dorahasin.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Camera;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;

public class Util {

    static String IMG_FILENAME = "IMG_";
    static String VIDEO_FILENAME = "VIDEO_";
    static String FOLDERNAME = "Rahasin";
    static String IMG_FOLDERNAME = "images";
    static String VIDEO_FOLDERNAME = "videos";
    FileOutputStream outputStream;
    static String folder = null;
    static File subFolder = null;

    public static String currentTimestampinString() {
        Long tsLong = System.currentTimeMillis() / 1000;
        return tsLong.toString();
    }

    public static Camera.Parameters getCameraParamWithRotation(Resources res, Camera.Parameters param) {
        if (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            param.set("orientation", "portrait");
            param.set("rotation", 90);
        } else if (res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            param.set("orientation", "landscape");
            param.set("rotation", 90);
        }
        return param;
    }

    public static void writeImgFilesToDir(byte[] data, Context context) {
        try {
            File sdCard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File dir = new File(sdCard.getAbsolutePath() + File.separator + FOLDERNAME + File.separator + IMG_FOLDERNAME);
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(dir, IMG_FILENAME + currentTimestampinString() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(data);
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File getOutputFile() {
        File file = null;
        try {
            File sdCard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File dir = new File(sdCard.getAbsolutePath() + File.separator + FOLDERNAME + File.separator + VIDEO_FOLDERNAME);
            if (!dir.exists())
                dir.mkdirs();
            file = new File(dir, VIDEO_FILENAME + currentTimestampinString() + ".mp4");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public static String getOutputFileStr() {
        File file = null;
        try {
            File sdCard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File dir = new File(sdCard.getAbsolutePath() + File.separator + FOLDERNAME + File.separator + VIDEO_FOLDERNAME);
            if (!dir.exists())
                dir.mkdirs();
            file = new File(dir, VIDEO_FILENAME + currentTimestampinString() + ".mp4");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }
}
