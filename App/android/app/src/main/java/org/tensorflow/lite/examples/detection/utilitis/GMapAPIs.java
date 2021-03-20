package org.tensorflow.lite.examples.detection.utilitis;

import android.content.Context;

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

public class GMapAPIs {
    String api_key = "";
    Context context;
    RequestQueue queue;

    public GMapAPIs(Context ctx, String api_key) {
        this.context = ctx;
        this.api_key = api_key;
        queue = Volley.newRequestQueue(context);
    }

    public void getStreetObject(String keyword, CallBack<StreetObjectGMaps> callBack) {
        String sub_key = "ho chi minh";

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
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + location_string + "&radius=" + range + "&type=restaurant&keyword=&key=" + api_key;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    ArrayList<PlaceObjectGMaps> list = new ArrayList<PlaceObjectGMaps>();
                    String obj_data_json = "";
                    try {
                        JSONObject jsonRoot = new JSONObject(response);
                        String status = jsonRoot.getString("status");
                        if (status.equals("OK")) {
                            JSONArray places = jsonRoot.getJSONArray("results");
                            for (int i = 0; i < places.length(); i++) {
                                JSONObject place = places.getJSONObject(i);
                                PlaceObjectGMaps obj = PlaceObjectGMaps.CreateNewFromJson(place.toString());
                                list.add(obj);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    callBack.SuccessListener(list);
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
