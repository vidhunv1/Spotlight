package com.stairway.spotlight.core.lib;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.TypedValue;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.stairway.spotlight.R;
import com.stairway.spotlight.UserSessionManager;
import com.stairway.spotlight.application.SpotlightApplication;
import com.stairway.spotlight.models.UserSession;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by vidhun on 03/01/17.
 */

public class ImageUtils {
    public static File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }

    //textsize in dp
    public static Drawable getDefaultProfileImage(String name, String key, double textSize) {
        if(name == null) {
            name = "#";
        }
        if(key == null) {
            key = "1";
        }
        ColorGenerator defaultColors = ColorGenerator.create(Arrays.asList(
                0xff5fbed5,
                0xff76c84d,
                0xff8e85ee,
                0xfff274a9,
                0xff549cdd,
                0xfff2749a,
                0xfff28c48,
                0xffe56555
        ));

        int count = 1;
        if(name==null || name.isEmpty()) {
            name = "#";
        }
        StringBuilder sb = new StringBuilder();
        for(String s : name.split(" ")){
            sb.append(s.charAt(0));
            if(count==2)
                break;
            count++;
        }

        Context context = SpotlightApplication.getContext();

        final int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)textSize, context.getResources().getDisplayMetrics());
        return TextDrawable.builder()
                .beginConfig()
                .textColor(Color.WHITE)
                .useFont(Typeface.createFromAsset(context.getResources().getAssets(), "fonts/roboto-medium.ttf"))
                .fontSize(px)
                .toUpperCase()
                .endConfig()
                .buildRound(sb.toString(), defaultColors.getColor(key));

    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
