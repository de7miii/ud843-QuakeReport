package com.example.android.quakereport;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Helper methods related to requesting and receiving earthquake data from USGS.
 */
public final class QueryUtils {

    private static final String TAG = QueryUtils.class.getSimpleName();
    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Return a list of {@link Earthquake} objects that has been built up from
     * parsing a JSON response.
     */
    public static ArrayList<Earthquake> extractEarthquakes(String urlString) {

        try {
            Thread.sleep(2000);
        }catch (InterruptedException e){
            Log.e(TAG, "extractEarthquakes: ", e);
        }

        // Create an empty ArrayList that we can start adding earthquakes to
        ArrayList<Earthquake> earthquakes = new ArrayList<>();
        String jsonResponse = "";

        try {
            jsonResponse = makeHttpRquest(urlString);
        }catch (IOException e){
            Log.e(TAG, "Error fetching data from url: ", e);
        }

        try {
            if (TextUtils.isEmpty(jsonResponse)){
                return null;
            }
            JSONObject root = new JSONObject(jsonResponse);
            JSONArray features = root.getJSONArray("features");

            for (int i = 0; i < features.length(); i++) {
                JSONObject current = features.getJSONObject(i);
                JSONObject properties = current.getJSONObject("properties");

                double mag = properties.getDouble("mag");
                String place = properties.getString("place");
                long time = properties.getLong("time");

                String magToDisplay = formatMagnitude(mag);
                String timeToDisplay = formatDate(time);

                Earthquake earthquake = new Earthquake(magToDisplay, place, timeToDisplay);
                earthquake.setWebUrl(properties.getString("url"));
                earthquakes.add(earthquake);
            }

        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }

        // Return the list of earthquakes
        return earthquakes;
    }

    private static String makeHttpRquest(String urlString) throws IOException{
        if (TextUtils.isEmpty(urlString)){
            return null;
        }
        URL url = createUrl(urlString);
        String jsonResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            assert url != null;
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /*milliseconds*/);
            urlConnection.setConnectTimeout(15000 /*milliseconds*/);
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200){
                inputStream = urlConnection.getInputStream();
                jsonResponse = readInput(inputStream);
            }else {
                Log.e(TAG, "Error getting data from server: " + urlConnection.getResponseCode(),null );
            }
        }catch (IOException e){
            Log.e(TAG, "Error making Http Request: ", e);
        }finally {
            if (urlConnection != null){
                urlConnection.disconnect();
            }
            if (inputStream != null){
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readInput(InputStream inputStream) throws IOException{
        if (inputStream == null){
            return null;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder output = new StringBuilder();
        try {
            String line = reader.readLine();
            while (line != null){
                output.append(line);
                line = reader.readLine();
            }
        }catch (IOException e){
            Log.e(TAG, "Error reading input stream: ", e);
        }
        return output.toString();
    }

    private static URL createUrl(String url) {
        if (TextUtils.isEmpty(url)){
            return null;
        }
        URL newUrl = null;
        try {
            newUrl = new URL(url);
        }catch (MalformedURLException e){
            Log.e(TAG, "Error creating URL: ",e);
        }
        return newUrl;
    }

    // helper method to format the time from UNIX time to locale format.
    private static String formatDate(long timeInMilliseconds){
        Date date = new Date(timeInMilliseconds);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM DD, yyyy \nh:mm:ss a", Locale.getDefault());
        return dateFormat.format(date);
    }

    private static String formatMagnitude(double magnitude){
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        return decimalFormat.format(magnitude);
    }
}