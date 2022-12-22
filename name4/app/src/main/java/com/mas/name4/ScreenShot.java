package com.mas.name4;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScreenShot {
    // 获取指定Activity的截屏，保存到png文件
    private static Bitmap takeScreenShot(Activity activity) {
        // View是你需要截图的View
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();

        // 获取状态栏高度
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.height();
        //Log.i("TAG", "" + statusBarHeight);

        // 获取屏幕长和高
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay().getHeight();
        // 去掉标题栏
        // Bitmap b = Bitmap.createBitmap(b1, 0, 25, 320, 455);
        //Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height);
        Bitmap b = Bitmap.createBitmap(b1);
        view.destroyDrawingCache();
        //Log.v("hahahaha",Bitmap_And_String.bitmapToString(b));

        return b;
    }

    // 保存到sdcard
    private static void savePic(Bitmap b, String strFileName) {

        FileOutputStream fos = null;
        Log.v("pathhh",fos+"");
        try {
            fos = new FileOutputStream(strFileName);
            Log.v("pathhh",strFileName+"");
            if (null != fos) {
                b.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            Log.v("pathhh","!!!!filenotfound!!!!");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    // 程序入口
    public static String shoot(Activity a) {
        //String strFileName = "/sdcard/" + String.valueOf(System.currentTimeMillis()) + ".png";
        String strFileName = String.valueOf(System.currentTimeMillis()) + ".PNG";
        Log.v("pathhh",strFileName+"");
        //savePic(takeScreenShot(a), strFileName);
        ImageSaveUtil.saveAlbum(a.getApplicationContext(),takeScreenShot(a),Bitmap.CompressFormat.PNG,90,strFileName,true);
        Log.v("pathhh",Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + strFileName+"");
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + strFileName;
    }


}