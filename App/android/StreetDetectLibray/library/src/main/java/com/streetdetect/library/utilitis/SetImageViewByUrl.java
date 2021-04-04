package com.streetdetect.library.utilitis;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

public class SetImageViewByUrl extends AsyncTask<String, Void, Bitmap> {

    ImageView bmImage;

    public SetImageViewByUrl(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String url_display = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(url_display).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }
}