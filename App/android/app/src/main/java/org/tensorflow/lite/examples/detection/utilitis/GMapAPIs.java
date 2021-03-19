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

    public void getStreetObject(String keyword, CallBack callBack) {
        String sub_key = "ho chi minh";

        String url = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=" + keyword + " " + sub_key + "&inputtype=textquery&fields=formatted_address,name,geometry&key=" + api_key;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response) {
                    String obj_data_json = "";
                    try
                    {
                        JSONObject jsonRoot = new JSONObject(response);
                        String status = jsonRoot.getString("status");
                        if (status.equals("OK")) {
                            JSONArray candidates = jsonRoot.getJSONArray("candidates");
                            obj_data_json = candidates.get(0).toString();
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }

                    StreetObjectGMaps obj = StreetObjectGMaps.CreateNewFromJson(obj_data_json);
                    callBack.SuccessListener(obj);
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error) {
                    callBack.FailureListener(error.getMessage());
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

    public interface CallBack {
        public abstract void SuccessListener(StreetObjectGMaps street);
        public abstract void FailureListener(String error);
    }
}
