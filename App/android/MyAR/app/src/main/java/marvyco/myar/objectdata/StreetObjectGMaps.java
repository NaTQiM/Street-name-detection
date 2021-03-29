package marvyco.myar.objectdata;

import org.json.JSONException;
import org.json.JSONObject;

public class StreetObjectGMaps {
    public final String formatted_address;
    public final Geometry geometry;
    public final String name;

    protected StreetObjectGMaps(String name, String formatted_address, Geometry geometry) {
        this.name = name;
        this.formatted_address = formatted_address;
        this.geometry = geometry==null?new Geometry():geometry;
    }

    public static StreetObjectGMaps CreateNewFromJson(String json) {
        String formatted_address = "???";
        Geometry geometry = null;
        String name = "???";
        try
        {
            JSONObject jsonRoot = new JSONObject(json);
            name = jsonRoot.getString("name");
            geometry = Geometry.NewFromJson(jsonRoot.getString("geometry"));
            formatted_address = jsonRoot.getString("formatted_address");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return new StreetObjectGMaps(name, formatted_address, geometry);
    }
}
