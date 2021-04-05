package com.streetdetect.library.objectdata;

import org.json.JSONException;
import org.json.JSONObject;

public class StreetName {

    private String name = "??";
    private String description = "Info: ???";

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "StreetName: " + name + " - " + description;
    }

    public static StreetName createNewFromJson(String json)
    {
        StreetName streetName = new StreetName();
        try
        {
            JSONObject jsonRoot = new JSONObject(json);
            streetName.setName(jsonRoot.getString("name"));
            streetName.setDescription(jsonRoot.getString("description"));
        }
       catch (JSONException e)
       {
            e.printStackTrace();
       }

        return streetName;
    }
}
