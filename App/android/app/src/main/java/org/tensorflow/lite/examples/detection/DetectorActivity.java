/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.detection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.location.Location;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.SystemClock;
import android.util.Pair;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.gson.JsonIOException;
import com.google.mlkit.vision.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.examples.detection.customview.OverlayView;
import org.tensorflow.lite.examples.detection.customview.OverlayView.DrawCallback;
import org.tensorflow.lite.examples.detection.env.BorderedText;
import org.tensorflow.lite.examples.detection.env.ImageUtils;
import org.tensorflow.lite.examples.detection.env.Logger;
import org.tensorflow.lite.examples.detection.objectdata.PlaceObjectGMaps;
import org.tensorflow.lite.examples.detection.objectdata.StreetObjectGMaps;
import org.tensorflow.lite.examples.detection.tflite.Detector;
import org.tensorflow.lite.examples.detection.tflite.TFLiteObjectDetectionAPIModel;
import org.tensorflow.lite.examples.detection.tracking.MultiBoxTracker;
import org.tensorflow.lite.examples.detection.utilitis.GMapAPIs;
import org.tensorflow.lite.examples.detection.utilitis.LevenshteinDistanceDP;
import org.tensorflow.lite.examples.detection.utilitis.SetImageViewByUrl;
import org.tensorflow.lite.examples.detection.utilitis.Tupple;

import java.text.Normalizer;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
    public static final Logger LOGGER = new Logger();

    // Configuration values for the prepackaged SSD model.
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
    private static final String TF_OD_API_LABELS_FILE = "labelmap.txt";
    private static final String STREET_NAME_DATA_FILE = "streetnames.json";
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.70f;
    private static final float MINIMUM_CONFIDENCE_TEXT_GEN = 0.70f;
    private static final int DETECTOR_NUM_THREAD = 4;
    private static final boolean MAINTAIN_ASPECT = false;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;

    private OverlayView trackingOverlay;
    private Integer sensorOrientation;

    private Detector detector;

    private long lastProcessingTimeMs;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;

    private boolean computingDetection = false;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private MultiBoxTracker tracker;

    private BorderedText borderedText;

    TextGeneration textGeneration;
    Map<String, String> streetnames_data = new HashMap<>();


    Tupple<StreetName, Integer> result_counter;
    private String temp_data = "";

    private int check_for_load_street_nearby = 0;
    private StreetObjectGMaps streetObjectGMaps = null;
    private Location userLocation = null;
    StreetName streetName;
    long timeLoadNearby;

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);


        tracker = new MultiBoxTracker(this);

        int cropSize = TF_OD_API_INPUT_SIZE;

        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            this,
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing Detector!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Detector could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

        result_counter = new Tupple<StreetName, Integer>(new StreetName(), 0);

        detector.setNumThreads(DETECTOR_NUM_THREAD);
        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                new DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        tracker.draw(canvas);
                        if (isDebug()) {
                            tracker.drawDebug(canvas);
                        }
                    }
                });

        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);

        String street_name_string = loadJSONFromAsset(STREET_NAME_DATA_FILE);
        try {
            JSONObject jsonObject = new JSONObject(street_name_string);
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (jsonObject.get(key) instanceof JSONObject) {
                    String str = ((JSONObject) jsonObject.get(key)).toString();
                    streetnames_data.put(key, str);
                }
            }
        } catch (JsonIOException | JSONException e) {
            e.printStackTrace();
        }


        textGeneration = new TextGeneration();
        textGeneration.AddListener(new TextGeneration.TextGenCallback() {
            @Override
            public void CallBackSuccess(Text text) {
                String label = text.getText().toUpperCase()
                        .replace("\n", " ");
                textGenCallBack(label);
            }

            @Override
            public void CallBackFailure(@NonNull Exception e) {

            }
        });

//        LOGGER.i("-----------------------------------------");
//        textGenCallBack("đương Nguyễn vên dấu");
//        LOGGER.i("-----------------------------------------");
//        textGenCallBack("đương Nguyễn vên dấu");
//        LOGGER.i("-----------------------------------------");
//        textGenCallBack("đương Nguyễn vên dấu");
//        LOGGER.i("-----------------------------------------");
//        textGenCallBack("đương Nguyễn vên dấu");
//        LOGGER.i("-----------------------------------------");
//        textGenCallBack("đương Nguyễn vên dấu");
//        LOGGER.i("-----------------------------------------");
//        textGenCallBack("đương Nguyễn vên dấu");
    }

    private void textGenCallBack(String label) {

        boolean is_contains_duong = label.contains("DUONG");
        String new_label = is_contains_duong ? deAccent(label.replace("DUONG", "")) : deAccent(label);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (streetnames_data.isEmpty())
                    return;

                Pair<Integer, String> temp = new Pair<Integer, String>(-1, null);
                for (Map.Entry<String, String> item : streetnames_data.entrySet()) {
                    String key = is_contains_duong ? item.getKey().replace("DUONG ", "") : item.getKey();
                    int compute = LevenshteinDistanceDP.compute(key.toLowerCase(), new_label.toLowerCase());
                    LOGGER.i("Compare: " + item.getKey() + " - " + new_label + " => " + compute);
                    if (temp.first < compute) {
                        temp = new Pair<Integer, String>(compute, item.getKey());
                    }
                }
                int min = (int) (MINIMUM_CONFIDENCE_TEXT_GEN * 100f);
                String streetname_json = temp.first < min ? "{}" : streetnames_data.get(temp.second);

                streetName = StreetName.createNewFromJson(streetname_json);

                if (streetName.getName().equals(result_counter.first.getName()))
                    result_counter.second++;
                else {
                    result_counter.first = streetName;
                    result_counter.second = 0;
                }

                if (result_counter.second >= 5) {
                    LOGGER.i("---------------------------------------------------");
                    RunDetector = false;
                    temp_data = streetName.getName();
                    runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {

                                detectResult.setText("Đường\n" + result_counter.first.getName());
                                detectResult.setVisibility(View.VISIBLE);
                                result_counter.first = new StreetName();
                                result_counter.second = 0;
                            }
                        });
                }

                tracker.SetLabel(streetName.getName());
            }
        }).start();
    }

    @Override
    protected void processImage() {
//        LOGGER.i("TIMESTAMP: " + timestamp);
        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;
        LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }
        if (!RunDetector)
            return;

        runInBackground(
            new Runnable() {
                @Override
                public void run() {
                    LOGGER.i("Running detection on image " + currTimestamp);
                    final long startTime = SystemClock.uptimeMillis();
                    final List<Detector.Recognition> results = detector.recognizeImage(croppedBitmap);
                    lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                    cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                    final Canvas canvas = new Canvas(cropCopyBitmap);
                    final Paint paint = new Paint();
                    paint.setColor(Color.RED);
                    paint.setStyle(Style.STROKE);
                    paint.setStrokeWidth(2.0f);

                    float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                    switch (MODE) {
                        case TF_OD_API:
                            minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                            break;
                    }

                    final List<Detector.Recognition> mappedRecognitions =
                            new ArrayList<Detector.Recognition>();

                    for (final Detector.Recognition result : results) {
                        final RectF location = result.getLocation();
                        if (location != null && result.getConfidence() >= minimumConfidence) {
                            Bitmap recognitionBitmap = cropBitmap(croppedBitmap, location);
                            textGeneration.Generate(recognitionBitmap, 0);

                            canvas.drawRect(location, paint);
                            cropToFrameTransform.mapRect(location);

                            result.setLocation(location);
                            mappedRecognitions.add(result);
                            break;
                        }
                    }

                    //tracker.trackResults(mappedRecognitions, currTimestamp);
                    trackingOverlay.postInvalidate();

                    computingDetection = false;

                    runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                }
            });
    }

    @Override
    protected void ShowData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detectResult.setText("Loading...");

            }
        });
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

        gpsManager.GetLocation(new GPSManager.OnGetLocation() {
            @Override
            public void Success(Location location) {
                LOGGER.i(" >>>>>>>>>>>>>>>>>>>>>> GPS " + location.getAltitude() + " : " + location.getLongitude());
                userLocation = location;
                //loadStreetNearby();
            }

            @Override
            public void Failure() {
                loadStreetNearby();
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.tfe_od_camera_connection_fragment_tracking;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum DetectorMode {
        TF_OD_API;
    }

    @Override
    protected void setUseNNAPI(final boolean isChecked) {
        runInBackground(
            () -> {
                try {
                    detector.setUseNNAPI(isChecked);
                } catch (UnsupportedOperationException e) {
                    LOGGER.e(e, "Failed to set \"Use NNAPI\".");
                    runOnUiThread(
                        () -> {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                }
            });
    }


    public String loadJSONFromAsset(String file) {
        String json = "{\"null\":\"empty\"}";
        try {
            InputStream is = getAssets().open(file);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

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
            LOGGER.i("<<<<<<" + first_address);
            GMAP_APIS.getStreetObject(first_address, new GMapAPIs.CallBack<StreetObjectGMaps>() {
                @Override
                public void SuccessListener(StreetObjectGMaps result) {
                    Double _lat = Double.parseDouble(streetObjectGMaps.geometry.location.lat);
                    Double _lng = Double.parseDouble(streetObjectGMaps.geometry.location.lng);
                    Tupple location = new Tupple(_lat, _lng);
                    Tupple origin = new Tupple(_lat, _lng);
                    HashMap<String, PlaceObjectGMaps> list_nearby = new HashMap();
                    LOGGER.i("temp_data: " + temp_data);


                    getPlaceNearbyRecursive(_this, list_nearby, location, origin, temp_data, -1);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            streetViewName.setText(streetObjectGMaps.name);
                            detectResult.setVisibility(View.INVISIBLE);
                            streetView.setVisibility(View.VISIBLE);
                        }
                    });

                }

                @Override
                public void FailureListener(String error) {

                }
            });

            GMAP_APIS.getNearby(location, 1500, new GMapAPIs.CallBack<ArrayList<PlaceObjectGMaps>>() {
                @Override
                public void SuccessListener(ArrayList<PlaceObjectGMaps> result) {
                    LOGGER.i(">>>>>>>>>>>>>>>>>" + result.size());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (PlaceObjectGMaps place: result) {
                                LinearLayout placeObjectUI = (LinearLayout) View.inflate(_this, R.layout.street_object, null);

                                LOGGER.i(">>>>>>>>>>>>>>>>> icon " + place.icon);
                                new SetImageViewByUrl((ImageView)placeObjectUI.findViewById(R.id.imageView)).execute(place.icon);
                                TextView textView = (TextView)placeObjectUI.findViewById(R.id.textView);
                                textView.setText(place.name);
                                listNearByPlaces.addView(placeObjectUI);
                            }
                            streetViewName.setText(streetObjectGMaps.name);
                            detectResult.setVisibility(View.INVISIBLE);
                            streetView.setVisibility(View.VISIBLE);
                        }
                    });

                }

                @Override
                public void FailureListener(String error) {
                    LOGGER.i("ERROR" + error);
                    backToScanStreetNameSign();
                    userLocation = null;
                }
            });
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
        GMAP_APIS.getNearby(location.first + "," + location.second, 180, new GMapAPIs.CallBack<ArrayList<PlaceObjectGMaps>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void SuccessListener(ArrayList<PlaceObjectGMaps> result) {

                Tupple<Double, Tupple<Double, Double>> max_location = new Tupple(distance(orginal, location), location);
                //LOGGER.i(orginal + " - " + location + " = "+ max_location.toString());

                for (PlaceObjectGMaps place : result) {
                    //LOGGER.i(place.vicinity);

                    String normalized_address = deAccent(place.vicinity).toLowerCase();

                    if (normalized_address.contains(deAccent(street).toLowerCase())) {
                        try {
                            Integer address_num = Integer.parseInt(normalized_address.split(",")[0].replace(deAccent(street).toLowerCase(),"").replace(" ",""));
                            Double _lat = Double.parseDouble(place.geometry.location.lat);
                            Double _lng = Double.parseDouble(place.geometry.location.lng);
                            Tupple<Double, Double> current_location = new Tupple(_lat, _lng);
                            Double current_distance = distance(orginal, current_location);
                            //LOGGER.i(current_distance.toString() + " - " + current_location);
                            if (current_distance > max_location.first)
                            {
                                max_location.first = current_distance;
                                max_location.second = current_location;
                            }
                        }
                        catch (Exception error) {
                            //error.printStackTrace();
                            }

                        if (!list.containsKey(place.place_id)) {
                            temp_list.put(place.place_id, place);
                            //list.put(place.place_id, place);

                            //LOGGER.i("Nearby: >> " + normalized_address);

                            if (limit >= 0 && list.size() >= limit)
                            {
                                //finishGetNearby(list);
                                return;
                            }
                        }
                    }
                    else
                    {
                        //LOGGER.i(" >> " + normalized_address + " - " + deAccent(street).toLowerCase());
                    }
                }
                //LOGGER.i("Max: >> " + max_location.second + " - " + max_location.first + "|" + location);
                updateGetNearbyToUI(temp_list);
                list.putAll(temp_list);
                if (!max_location.second.equals(location))
                    getPlaceNearbyRecursive(ctx, list, max_location.second, orginal, street, limit);
                else
                {
                    LOGGER.i(" >>>> TIME: " +  (System.currentTimeMillis()) + ", "+ (timeLoadNearby) + " = " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - timeLoadNearby));
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
        LOGGER.i("------------------------------------------------------------------------------");
//        for (Map.Entry<String, PlaceObjectGMaps> place: list.entrySet()) {
//            LOGGER.i(place.getValue().vicinity);
//        }
        Context _this = this;
        LOGGER.i(">>>>>>>>>>>>>>>>> Size: " + list.size());
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

    private Double distance(Tupple<Double, Double> v1, Tupple<Double, Double> v2)
    {
        return Math.sqrt(Math.pow(v1.first*10000 -v2.first*10000,2) + Math.pow(v1.first*10000-v2.first*10000,2));
    }
    @Override
    protected void backToScanStreetNameSign() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                streetView.setVisibility(View.INVISIBLE);
                RunDetector = true;
                computingDetection = false;
            }
        });
    }

    public static Bitmap cropBitmap(Bitmap bitmap, RectF size) {
        Rect box = new Rect();
        box.left = (int) size.left;
        box.top = (int) size.top;
        box.right = (int) size.right;
        box.bottom = (int) size.bottom;

        Bitmap recognitionBitmap = TextGeneration.cropBitmap(bitmap, box);
        return recognitionBitmap;
    }

}
