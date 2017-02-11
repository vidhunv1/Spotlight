package com.stairway.spotlight.core.lib;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.TypedValue;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.stairway.spotlight.R;
import com.stairway.spotlight.application.SpotlightApplication;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
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

    public static Drawable getDefaultProfileImage(String name, String key, double textSize) {
        int count = 1;
        StringBuilder sb = new StringBuilder();
        for(String s : name.split(" ")){
            sb.append(s.charAt(0));
            if(count==2)
                break;
            count++;
        }

        Context context = SpotlightApplication.getContext();

        final int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)textSize, context.getResources().getDisplayMetrics());

        TextDrawable textDrawable = TextDrawable.builder()
                .beginConfig()
                .textColor(Color.WHITE)
                .useFont(Typeface.createFromAsset(context.getResources().getAssets(), "fonts/roboto-medium.ttf"))
                .fontSize(px) /* size in px */
                .toUpperCase()
                .endConfig()
                .buildRound(sb.toString(), ColorGenerator.DEFAULT.getColor(key));

        return textDrawable;
    }
}
