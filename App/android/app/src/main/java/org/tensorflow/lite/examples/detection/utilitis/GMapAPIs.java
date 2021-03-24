package org.tensorflow.lite.examples.detection.utilitis;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.examples.detection.CameraActivity;
import org.tensorflow.lite.examples.detection.objectdata.Geometry;
import org.tensorflow.lite.examples.detection.objectdata.PlaceObjectGMaps;
import org.tensorflow.lite.examples.detection.objectdata.StreetObjectGMaps;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class GMapAPIs {
    String api_key = "";
    final String api_2 = "AIzaSyCKjiFuooUALilsYdPp8Lsgz3glE6Xig-s";
    Context context;
    RequestQueue queue;
    String city = "ho chi minh";

    RandomString rand;

    public GMapAPIs(Context ctx, String api_key) {
        this.context = ctx;
        this.api_key = api_key;
        queue = Volley.newRequestQueue(context);
        rand = new RandomString();
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void getStreetObject(String keyword, CallBack<StreetObjectGMaps> callBack) {
        String sub_key = city;

        String url = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=" + keyword + " " + sub_key + "&inputtype=textquery&fields=formatted_address,name,geometry&key=" + api_key;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String obj_data_json = "";
                        try {
                            JSONObject jsonRoot = new JSONObject(response);
                            String status = jsonRoot.getString("status");
                            if (status.equals("OK")) {
                                JSONArray candidates = jsonRoot.getJSONArray("candidates");
                                obj_data_json = candidates.get(0).toString();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        StreetObjectGMaps obj = StreetObjectGMaps.CreateNewFromJson(obj_data_json);
                        callBack.SuccessListener(obj);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callBack.FailureListener(error.getMessage());
            }
        }
        );
        queue.add(stringRequest);
    }

    public void getNearby(String location_string, Integer range, CallBack<ArrayList<PlaceObjectGMaps>> callBack) {

        //Log.i(">>>>>>>>>> ", "getNearby");
        ArrayList<PlaceObjectGMaps> list = new ArrayList<PlaceObjectGMaps>();
        process_recursive(location_string, range, callBack, list, "", 0);
    }

    private void process_recursive(final String location_string, final Integer range, final CallBack<ArrayList<PlaceObjectGMaps>> callBack, ArrayList<PlaceObjectGMaps> list, String next_page_token, int retry) {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + location_string +
                "&radius=" + range +
                "&type=restaurant&keyword=&key=" + (retry%2==1?api_key:api_2) +
                (next_page_token.length() > 0 ? "&pagetoken=" + next_page_token : "") +
                "&app=" + rand.nextString();
        //Log.i("getPage > " + retry, url);
        // Request a string response from the provided URL.
        if (retry > 20) {
            callBack.SuccessListener(list);
            return;
        }
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    //Log.i("Result", response);
                    String obj_data_json = "";
                    JSONObject jsonRoot = null;
                    try {
                        jsonRoot = new JSONObject(response);
                    } catch (JSONException e) { }

                    String page_token = "";
                    if (jsonRoot != null) {

                        String status = "ERROR";
                        try {
                            status = jsonRoot.getString("status");
                        } catch (JSONException e) { }

                        if (status.equals("OK")) {
                            try {
                                page_token = jsonRoot.getString("next_page_token");
                            } catch (Exception error) {}

                            JSONArray places = new JSONArray();
                            try {
                                places = jsonRoot.getJSONArray("results");
                            } catch (Exception e) {}

                            for (int i = 0; i < places.length(); i++) {
                                JSONObject place = new JSONObject();
                                try {
                                    place = places.getJSONObject(i);
                                } catch (Exception e) {}
                                PlaceObjectGMaps obj = PlaceObjectGMaps.CreateNewFromJson(place.toString());
                                list.add(obj);
                            }
                        } else if (status.equals("INVALID_REQUEST")) {
                            try {
                                TimeUnit.MILLISECONDS.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            process_recursive(location_string, range, callBack, list, next_page_token, retry + 1);
                            return;
                        }


                        if (page_token.length() == 0) {
                            callBack.SuccessListener(list);
                        } else
                            process_recursive(location_string, range, callBack, list, page_token, 0);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    callBack.FailureListener(error.getMessage());
            }
        }
        );
        queue.add(stringRequest);

    }

    public interface CallBack<T> {
        public abstract void SuccessListener(T result);

        public abstract void FailureListener(String error);
    }
}
