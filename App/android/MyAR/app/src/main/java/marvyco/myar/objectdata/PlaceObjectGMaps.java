package marvyco.myar.objectdata;

import org.json.JSONException;
import org.json.JSONObject;

public class PlaceObjectGMaps {
    public final String place_id;
    public final String business_status;
    public final Geometry geometry;
    public final String name;
    public final String icon;
    public final float rating;
    public final String vicinity;
    public final String[] types;

    protected PlaceObjectGMaps(String place_id,
                               String business_status,
                               Geometry geometry,
                               String name,
                               float rating,
                               String vicinity,
                               String[] types,
                               String icon) {
        this.place_id = place_id;
        this.business_status = business_status;
        this.geometry = geometry;
        this.name = name;
        this.rating = rating;
        this.vicinity = vicinity;
        this.types = types;
        this.icon = icon;
    }

    public static PlaceObjectGMaps CreateNewFromJson(String json) {
        String place_id = "??";
        String business_status = "??";
        Geometry geometry = new Geometry();
        String name = "??";
        float rating = 0.0f;
        String vicinity = "??";
        String[] types = {"??"};
        String icon = "";

        //Log.i("json: ", json);

        JSONObject jsonRoot = null;
        try {
            jsonRoot = new JSONObject(json);
        } catch (JSONException e) {
            //e.printStackTrace();
        }

        if (jsonRoot!=null){
            try {
                name = jsonRoot.getString("name");
            } catch (JSONException e) {
                //e.printStackTrace();
            }
            try {
                icon = jsonRoot.getString("icon");
            } catch (JSONException e) {
                //e.printStackTrace();
            }
            try {
                place_id = jsonRoot.getString("place_id");
            } catch (JSONException e) {
                //e.printStackTrace();
            }
            try {
                business_status = jsonRoot.getString("business_status");
            } catch (JSONException e) {
                //e.printStackTrace();
            }
            try {
                rating = (float) jsonRoot.getDouble("rating");
            } catch (JSONException e) {
                //e.printStackTrace();
            }
            try {
                vicinity = jsonRoot.getString("vicinity");
            } catch (JSONException e) {
                //e.printStackTrace();
            }
            try {
                types = jsonRoot.getJSONArray("types").toString().split(",");
            } catch (JSONException e) {
                //e.printStackTrace();
            }
            try {
                geometry = Geometry.NewFromJson(jsonRoot.getString("geometry"));
            } catch (JSONException e) {
                //e.printStackTrace();
            }
        }


        return new PlaceObjectGMaps(
                place_id,
                business_status,
                geometry,
                name,
                rating,
                vicinity,
                types,
                icon);
    }

}
