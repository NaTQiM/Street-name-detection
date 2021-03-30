package marvyco.myar.utilitis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
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


        int w=box.right-box.left;
        int h=box.bottom-box.top;
        Bitmap ret = Bitmap.createBitmap(w, h, bitmap.getConfig());
        Canvas canvas = new Canvas(ret);
        canvas.drawBitmap(bitmap, -box.left, -box.top, null);
        return ret;
    }

    public static void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }
}
