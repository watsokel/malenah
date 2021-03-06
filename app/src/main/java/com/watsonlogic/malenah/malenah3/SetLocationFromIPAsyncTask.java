package com.watsonlogic.malenah.malenah3;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;


public class SetLocationFromIPAsyncTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private double lat = Double.NEGATIVE_INFINITY;
    private double lng = Double.NEGATIVE_INFINITY;
    private URL url;
    private HttpURLConnection urlConnection;
    private Location location;
    private double portlandORLat = 45.52;
    private double portlandORLng = -122.6819;

    public SetLocationFromIPAsyncTask(Context context, URL url, Location location) {
        this.url = url;
        this.location = location;
        this.context = context;
    }

    private Boolean parseJSONString(String jsonStr) {
        try {
            if (jsonStr != null) {
                JSONObject jsonObj = new JSONObject(jsonStr);
                if (jsonObj != null) {
                    Iterator<String> itr = jsonObj.keys();
                    while (itr.hasNext()) {
                        String key = itr.next();
                        if (key.equals("lat")) {
                            setLat(Double.parseDouble(jsonObj.get(key).toString()));
                            Log.i("LOCATION JSON (double) ", Double.toString(lat));
                            Log.i("LOCATION", "Location set from IP-API.com");
                        } else if (key.equals("lon")) {
                            setLng(Double.parseDouble(jsonObj.get(key).toString()));
                            Log.i("LOCATION JSON (double) ", Double.toString(lng));
                            Log.i("LOCATION", "Location set from IP-API.com");
                        }
                    }
                    if (getLat() > Double.NEGATIVE_INFINITY && getLat() > Double.NEGATIVE_INFINITY) {
                        return true;
                    }
                } else {
                    return false; //jsonObj is null
                }
            }
        } catch (JSONException e) {
            Log.e("parseJSONString()", "error");
        }
        return false;
    }


    private HttpURLConnection connectToURL() {
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            return urlConnection;
        } catch (IOException e) {
            Log.e("LOCATION (error)", "error opening connection");
            return null;
        }
    }

    private String retrieveJSON() throws IOException {
        try {
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String jsonStr = inputStream.readLine();
            Log.i("LOCATION (JSON)", jsonStr);
            return jsonStr;
        } catch (IOException e) {
            Log.e("LOCATION (JSON)", "error reading stream, internet connection maybe lost");
            //int status = urlConnection.getResponseCode();
            //InputStream error = urlConnection.getErrorStream();
        }
        return null;
    }

    private void setFailSafeLocation() {
        Log.i("LOCATION", "setting failsafe");
        setLat(portlandORLat);
        setLng(portlandORLng);
    }

    @Override
    protected String doInBackground(Void... params) {
        urlConnection = connectToURL();
        if (urlConnection != null) {
            String jsonStr = null;
            try {
                jsonStr = retrieveJSON();
                if (!parseJSONString(jsonStr)) {
                    setFailSafeLocation();
                }
            } catch (IOException e) {
                e.printStackTrace();
                setFailSafeLocation();
            }
        } else {
            setFailSafeLocation();
        }
        urlConnection.disconnect();
        return "done";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        location.setLatitude(getLat()); //set latitude
        location.setLongitude(getLng()); //set longitude
        ((DrawerActivity) this.context).locationDone(location);
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
