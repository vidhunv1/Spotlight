package com.stairway.spotlight.core.lib;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Environment;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

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

    public static Drawable getDefaultTextDP(String name, String key) {
        StringBuilder sb = new StringBuilder();
        for(String s : name.split(" ")){
            sb.append(s.charAt(0));
        }

        ColorGenerator generator = ColorGenerator.DEFAULT;

        TextDrawable textDrawable = TextDrawable.builder()
                .buildRound(sb.toString(), generator.getColor(key));

        return textDrawable;
    }
}
