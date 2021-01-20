package com.wshunli.map.tianditu.sample;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.MapView;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = TestActivity.class.getSimpleName();

    private MapView mMapView;
    // objects that implement Loadable must be class fields to prevent being garbage collected before loading
    private MobileMapPackage mMapPackage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // get a reference to the map view
        mMapView = findViewById(R.id.mapView);

        //[DocRef: Name=Open Mobile Map Package-android, Category=Work with maps, Topic=Create an offline map]
        // create the mobile map package
        //String mainMMPKPath = Environment.getExternalStorageDirectory()+"/ArcGIS/SanFrancisco.mmpk";
        String mainMMPKPath = Environment.getExternalStorageDirectory()+"/ArcGIS/123r.mmpk";
        mMapPackage = new MobileMapPackage(mainMMPKPath);
        // load the mobile map package asynchronously
        mMapPackage.loadAsync();

        // add done listener which will invoke when mobile map package has loaded
        mMapPackage.addDoneLoadingListener(() -> {
            // check load status and that the mobile map package has maps
            if (mMapPackage.getLoadStatus() == LoadStatus.LOADED && !mMapPackage.getMaps().isEmpty()) {
                // add the map from the mobile map package to the MapView
                mMapView.setMap(mMapPackage.getMaps().get(0));
            } else {
                String error = "Error loading mobile map package: " + mMapPackage.getLoadError().getMessage();
                Log.e(TAG, error);
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
        //[DocRef: END]
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.dispose();
    }
}