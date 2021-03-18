package org.tensorflow.lite.examples.detection.objectdata;


import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class Geometry {
    public final Location location;
    public final ViewPort viewPort;

    public Geometry() {
        this.location = new Location();
        this.viewPort = new ViewPort();
    }

    protected Geometry(Location location, ViewPort viewPort) {
        this.location = location==null?new Location():location;
        this.viewPort = viewPort==null?new ViewPort():viewPort;
    }

    public static Geometry NewFromJson(String json) {
        Location location = null;
        ViewPort viewPort = null;
        try
        {
            JSONObject jsonRoot = new JSONObject(json);
            location = Location.NewFromJson(jsonRoot.getString("location"));
            viewPort = ViewPort.NewFromJson(jsonRoot.getString("viewPort"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return new Geometry(location, viewPort);
    }
}