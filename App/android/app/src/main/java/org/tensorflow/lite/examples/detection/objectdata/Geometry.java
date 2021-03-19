package org.tensorflow.lite.examples.detection.objectdata;


import org.json.JSONException;
import org.json.JSONObject;

public class Geometry {
    public final Location location;
    public final ViewPort viewport;

    public Geometry() {
        this.location = new Location();
        this.viewport = new ViewPort();
    }

    protected Geometry(Location location, ViewPort viewport) {
        this.location = location==null?new Location():location;
        this.viewport = viewport ==null?new ViewPort(): viewport;
    }

    public static Geometry NewFromJson(String json) {
        Location location = null;
        ViewPort viewPort = null;
        try
        {
            JSONObject jsonRoot = new JSONObject(json);
            location = Location.NewFromJson(jsonRoot.getString("location"));
            viewPort = ViewPort.NewFromJson(jsonRoot.getString("viewport"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return new Geometry(location, viewPort);
    }
}