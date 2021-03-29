package marvyco.myar;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import marvyco.myar.env.Logger;
import marvyco.myar.objectdata.PlaceObjectGMaps;
import marvyco.myar.objectdata.StreetObjectGMaps;
import marvyco.myar.utilitis.GMapAPIs;
import marvyco.myar.utilitis.SetImageViewByUrl;
import marvyco.myar.utilitis.Tupple;
import marvyco.myar.utilitis.Utility;


public class StreetViewActivity extends AppCompatActivity {

    private BottomSheetBehavior<LinearLayout> sheetBehavior;

    protected static final int REQUEST_PGS_PERMISSION_CODE = 2;
    protected static final String API_KEY = "AIzaSyAdPZ4-QUW2nsW8xswNQjB2lRoaBOPkO1s";
    protected GMapAPIs GMAP_APIS;
    protected GPSManager gpsManager;

    // UI
    protected LinearLayout bottomSheetLayout;
    protected LinearLayout gestureLayout;
    protected ImageView bottomSheetArrowImageView;
    protected Button buttonCloseStreetView;
    protected TextView streetViewName;
    protected ScrollView streetView;
    protected LinearLayout listNearByPlaces;

    private Logger LOGGER = new Logger();
    String temp_data;
    private StreetObjectGMaps streetObjectGMaps;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle param = getIntent().getExtras();
        temp_data = "??";
        try {
            if (param!=null)
            {
                temp_data = param.getString("street");
            }
        }
        catch (Exception e) { e.printStackTrace(); }

        gpsManager = new GPSManager(this, REQUEST_PGS_PERMISSION_CODE);

        streetView = (ScrollView)findViewById(R.id.street_view);
        buttonCloseStreetView = (Button) findViewById(R.id.close_street_view);
        streetViewName = (TextView) findViewById(R.id.street_view_name);

        listNearByPlaces = (LinearLayout) findViewById(R.id.scroll_view_result);

        bottomSheetLayout = findViewById(R.id.bottom_sheet_layout);
        gestureLayout = findViewById(R.id.gesture_layout);
        sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetArrowImageView = findViewById(R.id.bottom_sheet_arrow);

        ViewTreeObserver vto = gestureLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            gestureLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            gestureLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        // int width = bottomSheetLayout.getMeasuredWidth();
                        int height = gestureLayout.getMeasuredHeight();

                        sheetBehavior.setPeekHeight(height);
                    }
                });
        sheetBehavior.setHideable(false);

        sheetBehavior.addBottomSheetCallback(
                new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        switch (newState) {
                            case BottomSheetBehavior.STATE_HIDDEN:
                                break;
                            case BottomSheetBehavior.STATE_EXPANDED: {
                                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_down);
                            }
                            break;
                            case BottomSheetBehavior.STATE_COLLAPSED: {
                                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
                            }
                            break;
                            case BottomSheetBehavior.STATE_DRAGGING:
                                break;
                            case BottomSheetBehavior.STATE_SETTLING:
                                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
                                break;
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    }
                });

        GMAP_APIS = new GMapAPIs(this, API_KEY);

        buttonCloseStreetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToScanStreetNameSign();
            }
        });

    }

    private void backToScanStreetNameSign() {
        Intent intentStreetDetection = new Intent(StreetViewActivity.this, StreetDetectionActivity.class);
        startActivity(intentStreetDetection);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_PGS_PERMISSION_CODE:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    gpsManager.onGPSPermissionGranted(true);
                }  else {
                    gpsManager.onGPSPermissionGranted(false);
                }
                break;
        }
    }

    protected void ShowData() {
        GMAP_APIS.getStreetObject(temp_data, new GMapAPIs.CallBack<StreetObjectGMaps>() {
            @Override
            public void SuccessListener(StreetObjectGMaps street) {
                LOGGER.i(" >>>>>>>>>>>>>>>>>>>>>> " + street.formatted_address);
                streetObjectGMaps = street;
                loadStreetNearby();
            }

            @Override
            public void FailureListener(String error) {
                loadStreetNearby();
            }
        });

//        gpsManager.GetLocation(new GPSManager.OnGetLocation() {
//            @Override
//            public void Success(Location location) {
//                LOGGER.i(" >>>>>>>>>>>>>>>>>>>>>> GPS " + location.getAltitude() + " : " + location.getLongitude());
//                userLocation = location;
//                //loadStreetNearby();
//            }
//
//            @Override
//            public void Failure() {
//                loadStreetNearby();
//            }
//        });
    }
    int check_for_load_street_nearby; long timeLoadNearby;
    android.location.Location userLocation;
    private void loadStreetNearby() {

        check_for_load_street_nearby++;
        LOGGER.i(">>>>>>>>>>>>>>>>> i " + check_for_load_street_nearby);
        if (check_for_load_street_nearby == 1) {
            check_for_load_street_nearby = 0;
            timeLoadNearby = System.currentTimeMillis();
            String location = "";
            if (userLocation!=null)
            {
                location = userLocation.getLatitude() + "," + userLocation.getLongitude();
            }
            else if (streetObjectGMaps!=null){
                location = streetObjectGMaps.geometry.location.lat + "," + streetObjectGMaps.geometry.location.lng;
            }
            else
            {
                backToScanStreetNameSign();
                userLocation = null;
                return;
            }
            LOGGER.i(">>>>>>>>>>>>>>>>>>>>> LOCATION " + location);
            String first_address = "01 " + temp_data;
            final Context _this = this;
            LOGGER.i("<<<<<< " + first_address);
            GMAP_APIS.getStreetObject(first_address, new GMapAPIs.CallBack<StreetObjectGMaps>() {
                @Override
                public void SuccessListener(StreetObjectGMaps result) {
                    Double _lat = Double.parseDouble(result.geometry.location.lat);
                    Double _lng = Double.parseDouble(result.geometry.location.lng);
                    Tupple location = new Tupple(_lat, _lng);
                    Tupple origin = new Tupple(_lat, _lng);
                    HashMap<String, PlaceObjectGMaps> list_nearby = new HashMap();

                    LOGGER.i("temp_data: " + temp_data);


                    getPlaceNearbyRecursive(_this, list_nearby, location, origin, temp_data, -1);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            streetViewName.setText(streetObjectGMaps.name);
                            streetView.setVisibility(View.VISIBLE);
                        }
                    });

                }

                @Override
                public void FailureListener(String error) {

                }
            });

//            GMAP_APIS.getNearby(location, 150, new GMapAPIs.CallBack<ArrayList<PlaceObjectGMaps>>() {
//                @Override
//                public void SuccessListener(ArrayList<PlaceObjectGMaps> result) {
//                    LOGGER.i(">>>>>>>>>>>>>>>>> " + result.size());
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            for (PlaceObjectGMaps place: result) {
//                                LinearLayout placeObjectUI = (LinearLayout) View.inflate(_this, R.layout.street_object, null);
//
//                                //LOGGER.i(">>>>>>>>>>>>>>>>> icon " + place.icon);
//                                new SetImageViewByUrl((ImageView)placeObjectUI.findViewById(R.id.imageView)).execute(place.icon);
//                                TextView textView = (TextView)placeObjectUI.findViewById(R.id.textView);
//                                textView.setText(place.name);
//                                listNearByPlaces.addView(placeObjectUI);
//                            }
//                            streetViewName.setText(streetObjectGMaps.name);
//                            detectResult.setVisibility(View.INVISIBLE);
//                            streetView.setVisibility(View.VISIBLE);
//                        }
//                    });
//
//                }
//
//                @Override
//                public void FailureListener(String error) {
//                    LOGGER.i("ERROR" + error);
//                    backToScanStreetNameSign();
//                    userLocation = null;
//                }
//            });
        }
    }

    private void getPlaceNearbyRecursive(
            final Context ctx,
            HashMap<String, PlaceObjectGMaps> list,
            Tupple<Double, Double> location,
            final Tupple<Double, Double> orginal,
            final String street,
            final int limit)
    {
        HashMap<String, PlaceObjectGMaps> temp_list = new HashMap<>();
        GMAP_APIS.getNearby(location.first + "," + location.second, 80, new GMapAPIs.CallBack<ArrayList<PlaceObjectGMaps>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void SuccessListener(ArrayList<PlaceObjectGMaps> result) {

                Tupple<Double, Tupple<Double, Double>> max_location = new Tupple(Utility.distance(orginal, location), location);
                String max_address = "";
                //LOGGER.i(orginal + " - " + location + " = "+ max_location.toString());

                for (PlaceObjectGMaps place : result) {
                    //LOGGER.i(place.vicinity);

                    String normalized_address = Utility.deAccent(place.vicinity).toLowerCase();

                    if (normalized_address.contains(Utility.deAccent(street).toLowerCase())) {
                        String address_num_string = normalized_address.split(",")[0].replace(Utility.deAccent(street).toLowerCase(),"").replace(" ","");

                        if (address_num_string.matches("[0-9]+[a-zA-Z]?"))
                        {
                            Double _lat = Double.parseDouble(place.geometry.location.lat);
                            Double _lng = Double.parseDouble(place.geometry.location.lng);
                            Tupple<Double, Double> current_location = new Tupple(_lat, _lng);
                            Double current_distance = Utility.distance(orginal, current_location);
                            //LOGGER.i(current_distance.toString() + " - " + current_location);
                            if (current_distance > max_location.first)
                            {
                                max_location.first = current_distance;
                                max_location.second = current_location;
                                max_address = place.vicinity;
                            }
                        }

                        if (!list.containsKey(place.place_id)) {
                            temp_list.put(place.place_id, place);
                            //list.put(place.place_id, place);

                            LOGGER.i("Nearby: >> " + normalized_address);

                            if (limit >= 0 && list.size() >= limit)
                            {
                                //finishGetNearby(list);
                                return;
                            }
                        }
                        else
                        {
                            LOGGER.i(" >> " + normalized_address + " - " + Utility.deAccent(street).toLowerCase());
                        }
                    }
                    else
                    {
                        LOGGER.i(" >> " + normalized_address + " - " + Utility.deAccent(street).toLowerCase());
                    }
                }
                LOGGER.i("Max: >> " + max_location.second + " - " + max_location.first + " | " + max_address);
                updateGetNearbyToUI(temp_list);
                list.putAll(temp_list);
                if (!max_location.second.equals(location))
                    getPlaceNearbyRecursive(ctx, list, max_location.second, orginal, street, limit);
                else
                {
                    LOGGER.i(" >>>> TIME: " +  (System.currentTimeMillis()) + ", "+ (timeLoadNearby) + " = " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - timeLoadNearby));
                    LOGGER.i("------------------------------------------------------------------------------");
                    //        for (Map.Entry<String, PlaceObjectGMaps> place: list.entrySet()) {
                    //            LOGGER.i(place.getValue().vicinity);
                    //        }
                    LOGGER.i(">>>>>>>>>>>>>>>>> Size: " + list.size());
                    //finishGetNearby(list);
                    return;
                }
            }

            @Override
            public void FailureListener(String error) {
                backToScanStreetNameSign();
                userLocation = null;
            }
        });

    }

    private void updateGetNearbyToUI(HashMap<String, PlaceObjectGMaps> list) {
        Context _this = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<String, PlaceObjectGMaps> item: list.entrySet()) {

                    PlaceObjectGMaps place = item.getValue();

                    LinearLayout placeObjectUI = (LinearLayout) View.inflate(_this, R.layout.street_object, null);

                    //LOGGER.i(">>>>>>>>>>>>>>>>> icon " + place.icon);
                    new SetImageViewByUrl((ImageView)placeObjectUI.findViewById(R.id.imageView)).execute(place.icon);
                    TextView textView = (TextView)placeObjectUI.findViewById(R.id.textView);
                    textView.setText(place.name);
                    listNearByPlaces.addView(placeObjectUI);
                }
            }
        });
    }


}
