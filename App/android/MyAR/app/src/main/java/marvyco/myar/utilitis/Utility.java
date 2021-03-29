package marvyco.myar.utilitis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.regex.Pattern;

import marvyco.myar.TextGeneration;

public class Utility {
    public static Double distance(Tupple<Double, Double> v1, Tupple<Double, Double> v2)
    {
        return Math.sqrt(Math.pow(v1.first*10000 - v2.first*10000,2) + Math.pow(v1.first*10000 - v2.first*10000,2));
    }

    static public String loadJSONFromAsset(Context ctx, String file) {
        String json = "{\"null\":\"empty\"}";
        try {
            InputStream is = ctx.getAssets().open(file);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    static public String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    public static Bitmap cropBitmap(Bitmap bitmap, RectF size) {
        Rect box = new Rect();
        box.left = (int) size.left;
        box.top = (int) size.top;
        box.right = (int) size.right;
        box.bottom = (int) size.bottom;

        Bitmap recognitionBitmap = TextGeneration.cropBitmap(bitmap, box);
        return recognitionBitmap;
    }
}
