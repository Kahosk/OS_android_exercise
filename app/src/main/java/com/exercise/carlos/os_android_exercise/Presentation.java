package com.exercise.carlos.os_android_exercise;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


public class Presentation extends ActionBarActivity {

    private ImageView imgView;
    private String imageURL;

    // String to create Flickr API urls
    private static final String FLICKR_BASE_URL = "https://api.flickr.com/services/rest/?method=";
    private static final String FLICKR_PHOTOS_SEARCH_STRING = "flickr.photos.search";
    private static final String NUMBER_OF_PHOTOS = "&per_page=1&media=photos";

    //You can set here your API_KEY
    private static final String APIKEY_SEARCH_STRING = "&api_key=226d3ac270b6384fe6ed45c0d74c4372";

    private static final String TAGS_STRING = "&tags=cowmeat";
    private static final String FORMAT_STRING = "&format=json";

    //threads
    public static UIHandler uihandler;


    //Places
    int PLACE_PICKER_REQUEST = 1;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_presentation);
        uihandler = new UIHandler();

        imgView = (ImageView) findViewById(R.id.imageView1);
        new Thread(getMetadata).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_presentation, menu);

        return true;
    }

    private static String createURL() {

        String url = FLICKR_BASE_URL + FLICKR_PHOTOS_SEARCH_STRING + APIKEY_SEARCH_STRING + TAGS_STRING + FORMAT_STRING + NUMBER_OF_PHOTOS;
        return url;
    }

    // http://farm{farm-id}.staticflickr.com/{server-id}/{id}_{secret}.jpg
    public void getImageURLS(Context ctx) {
        String url = createURL();
        String json = null;
        try {
            if (URLConnector.isOnline(ctx)) {
                ByteArrayOutputStream baos = URLConnector.readBytes(url);
                json = baos.toString();
            }
            try {
                JSONObject root = new JSONObject(json.replace("jsonFlickrApi(", "").replace(")", ""));
                JSONObject photos = root.getJSONObject("photos");
                JSONArray imageJSONArray = photos.getJSONArray("photo");
                for (int i = 0; i < imageJSONArray.length(); i++) {
                    JSONObject image = imageJSONArray.getJSONObject(i);
                    imageURL = createPhotoURL(image.getString("farm"), image.getString("server"), image.getString("id"), image.getString("secret"));

                    Message msg = Message.obtain(uihandler);
                    Bitmap imageTemp = getImage(imageURL);
                    msg.obj = imageTemp;
                    uihandler.sendMessage(msg);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (NullPointerException nue) {
            nue.printStackTrace();
        }

    }

    public static Bitmap getImage(String imageURL) {
        Bitmap bm = null;
        try {
            URL aURL = new URL(imageURL);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (Exception e) {
            Log.e("getImage", e.getMessage());
        }
        return bm;
    }

    private String createPhotoURL(String farm, String server, String id, String secret) {
        String tmp = null;
        tmp = "http://farm" + farm + ".staticflickr.com/" + server + "/" + id + "_" + secret + "_z" + ".jpg";
        Log.e("createPhotoURL", tmp);
        return tmp;
    }

    //UIHandler for threads

    class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // Display large image
            if (msg.obj != null) {

                imgView.setImageBitmap((Bitmap) msg.obj);
                imgView.setVisibility(View.VISIBLE);
            }
            super.handleMessage(msg);
        }
    }

    /**
     * Runnable to get metadata from Flickr API
     */
    Runnable getMetadata = new Runnable() {
        @Override
        public void run() {
            getImageURLS(getApplicationContext());
        }
    };


    /** Called when the user clicks the Map button */
    public void mapLocations(View view) {
        /* Google Places API (
        int PLACE_PICKER_REQUEST = 1;
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        } */
        Intent intent = new Intent(this, MapsActivity.class);

        startActivity(intent);
    }

    /** Called when the user clicks the Twitter button */
    public void twiterFeed(View view) {
        Intent intent = new Intent(this, TwiterActivity.class);

        startActivity(intent);
    }
}


