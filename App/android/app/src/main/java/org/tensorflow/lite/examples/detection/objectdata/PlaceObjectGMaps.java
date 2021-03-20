package org.tensorflow.lite.examples.detection.objectdata;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class PlaceObjectGMaps {
    public final String place_id;
    public final String business_status;
    public final String formatted_address;
    public final Geometry geometry;
    public final String name;
    public final String icon;
    public final float rating;
    public final String vicinity;
    public final String[] type;

    protected PlaceObjectGMaps(String place_id,
                               String business_status,
                               String formatted_address,
                               Geometry geometry,
                               String name,
                               float rating,
                               String vicinity,
                               String[] type,
                               String icon) {
        this.place_id = place_id;
        this.business_status = business_status;
        this.formatted_address = formatted_address;
        this.geometry = geometry;
        this.name = name;
        this.rating = rating;
        this.vicinity = vicinity;
        this.type = type;
        this.icon = icon;
    }

    public static PlaceObjectGMaps CreateNewFromJson(String json) {
        String place_id = "??";
        String business_status = "??";
        String formatted_address = "??";
        Geometry geometry = new Geometry();
        String name = "??";
        float rating = 0.0f;
        String vicinity = "??";
        String[] type = {"??"};
        String icon = "";

        try {
            JSONObject jsonRoot = new JSONObject(json);
            name = jsonRoot.getString("name");
            icon = jsonRoot.getString("icon");
            place_id = jsonRoot.getString("place_id");
            business_status = jsonRoot.getString("business_status");
            formatted_address = jsonRoot.getString("formatted_address");
            rating = (float) jsonRoot.getDouble("rating");
            formatted_address = jsonRoot.getString("formatted_address");
            vicinity = jsonRoot.getString("vicinity");
            type = jsonRoot.getJSONArray("type").toString().split(",");
            geometry = Geometry.NewFromJson(jsonRoot.getString("geometry"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new PlaceObjectGMaps(
                place_id,
                business_status,
                formatted_address,
                geometry,
                name,
                rating,
                vicinity,
                type,
                icon);
    }

}
