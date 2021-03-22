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

public class GMapAPIs {
    String api_key = "";
    Context context;
    RequestQueue queue;
    String city = "ho chi minh";

    public GMapAPIs(Context ctx, String api_key) {
        this.context = ctx;
        this.api_key = api_key;
        queue = Volley.newRequestQueue(context);
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
        ArrayList<PlaceObjectGMaps> list = new ArrayList<PlaceObjectGMaps>();
        process_recursive(location_string, range, callBack, list, "");
    }

    private void process_recursive(final String location_string, final Integer range, final CallBack<ArrayList<PlaceObjectGMaps>> callBack, ArrayList<PlaceObjectGMaps> list, String next_page_token) {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+
                location_string + "&radius=" +
                range + "&type=restaurant&keyword=&key=" +
                api_key + (next_page_token.length() > 0?"&pageToken="+next_page_token:"");
        Log.i("getNearby > ", url);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        String obj_data_json = "";
                        String page_token = "";
                        try {
                            JSONObject jsonRoot = new JSONObject(response);
                            String status = jsonRoot.getString("status");
                            if (status.equals("OK")) {
                                if (response.contains("next_page_token"))
                                    page_token = jsonRoot.getString("next_page_token");

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

                        if (page_token.length() == 0)
                            callBack.SuccessListener(list);
                        else
                            process_recursive(location_string, range, callBack, list, page_token);

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
