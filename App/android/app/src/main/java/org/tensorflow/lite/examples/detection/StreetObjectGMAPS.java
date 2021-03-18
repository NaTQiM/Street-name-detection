package org.tensorflow.lite.examples.detection;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.examples.detection.objectdata.Geometry;

public class StreetObjectGMAPS {
    protected String formatted_address;
    protected Geometry geometry;
    protected String name;

    protected StreetObjectGMAPS() {

    }

    public static StreetObjectGMAPS CreateNewFromJson(String json) {
        StreetObjectGMAPS streetObjectGMAPS = new StreetObjectGMAPS();
        try
        {
            JSONObject jsonRoot = new JSONObject(json);
            streetObjectGMAPS.name = jsonRoot.getString("name");
            streetObjectGMAPS.geometry = Geometry.NewFromJson(jsonRoot.getString("geometry"));
            streetObjectGMAPS.formatted_address = jsonRoot.getString("formatted_address");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return streetObjectGMAPS;
    }
}
