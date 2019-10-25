/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EarthquakeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Earthquake>>, CustomAdapter.OnItemClickListener {

    public static final String TAG = EarthquakeActivity.class.getName();

    @BindView(R.id.rv_list)RecyclerView recyclerView;
    @BindView(R.id.text_empty) TextView emptyText;
    @BindView(R.id.progress_bar) ProgressBar progressBar;

    private ArrayList<Earthquake> mEarthquakes;
    CustomAdapter adapter;

    private final String USGS_API_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        ButterKnife.bind(this);

        adapter = new CustomAdapter(this, this);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        emptyText.setVisibility(View.INVISIBLE);

        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnectedOrConnecting()){
            getSupportLoaderManager().initLoader(0,null, this).forceLoad();
        }else{
            recyclerView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            emptyText.setText("No internet connection");
            emptyText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClicked(int position) {
        Earthquake earthquake = mEarthquakes.get(position);
        Uri webpage = Uri.parse(earthquake.getWebUrl());
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @NonNull
    @Override
    public Loader<List<Earthquake>> onCreateLoader(int id, @Nullable Bundle args) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String minMagnitude = sharedPref.getString(
                getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default));
        String orderBy = sharedPref.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
        Uri baseUri = Uri.parse(USGS_API_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter("format", "geojson");
        uriBuilder.appendQueryParameter("eventtype", "earthquake");
        uriBuilder.appendQueryParameter("orderby", orderBy);
        uriBuilder.appendQueryParameter("minmag", minMagnitude);
        uriBuilder.appendQueryParameter("limit", "10");
        return new EarthquakeAsyncLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Earthquake>> loader, List<Earthquake> data) {
        progressBar.setVisibility(View.INVISIBLE);
        if (data != null){
            if (!data.isEmpty()) {
                emptyText.setVisibility(View.INVISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
                mEarthquakes = (ArrayList<Earthquake>) data;
                adapter.updateEarthquakes(data);
                adapter.notifyDataSetChanged();
            }else{
                recyclerView.setVisibility(View.INVISIBLE);
                emptyText.setText("No earthquake found");
                emptyText.setVisibility(View.VISIBLE);
            }
        }else {
            recyclerView.setVisibility(View.INVISIBLE);
            emptyText.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Earthquake>> loader) {
        adapter.updateEarthquakes(new ArrayList<>());
    }

    private static class EarthquakeAsyncLoader extends AsyncTaskLoader<List<Earthquake>> {

        String urlString = "";

        public EarthquakeAsyncLoader(@NonNull Context context, String urlString) {
            super(context);
            this.urlString = urlString;
        }

        @Nullable
        @Override
        public List<Earthquake> loadInBackground() {
            if (urlString == null){
                return null;
            }
            ArrayList<Earthquake> earthquakes = null;
            earthquakes = QueryUtils.extractEarthquakes(urlString);
            return earthquakes;
        }
    }
}
