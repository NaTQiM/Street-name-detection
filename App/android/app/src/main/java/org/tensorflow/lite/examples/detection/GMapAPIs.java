package org.tensorflow.lite.examples.detection;

import android.content.Context;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.tensorflow.lite.examples.detection.objectdata.PlaceObjectGMaps;

import java.util.ArrayList;
import java.util.List;

public class GMapAPIs {
    String api_key = "";
    Context context;
    RequestQueue queue;
    public GMapAPIs(Context ctx, String api_key) {
        this.context = ctx;
        this.api_key = api_key;
        queue = Volley.newRequestQueue(context);
    }
    public void getStreetObject(String keyword, OnSuccess onSuccess, @Nullable OnFailure onFailure) {
        List<StreetObjectGMAPS> streetObjectGMAPS = new ArrayList<StreetObjectGMAPS>();

        String url = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=nguyen%20van%20dau%20ho%20chi%20minh&inputtype=textquery&fields=formatted_address,name,geometry&key=" + api_key;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response) {
                    streetObjectGMAPS.add(StreetObjectGMAPS.CreateNewFromJson(response));
                    onSuccess.Listener(streetObjectGMAPS.get(0));
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error) {
                    onFailure.Listener(error.getMessage());
                }
            }
        );
        queue.add(stringRequest);
    }

    public ArrayList<PlaceObjectGMaps> getNearby(String keyword) {
        ArrayList<PlaceObjectGMaps> list = new ArrayList<PlaceObjectGMaps>();
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=1500&type=restaurant&keyword=cruise&key=AIzaSyAdPZ4-QUW2nsW8xswNQjB2lRoaBOPkO1s";

        return list;
    }

    protected interface CallBack {

    }

    protected abstract class OnSuccess implements CallBack {
        abstract void Listener(StreetObjectGMAPS street);
    }

    protected abstract class OnFailure implements CallBack {
        abstract void Listener(String error);
    }
}
