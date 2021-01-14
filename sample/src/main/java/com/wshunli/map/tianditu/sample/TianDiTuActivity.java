/*
 * Copyright 2017 wshunli
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
package com.wshunli.map.tianditu.sample;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.location.LocationDataSource;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.wshunli.map.tianditu.TianDiTuLayer;
import com.wshunli.map.tianditu.TianDiTuLayerBuilder;

import java.io.File;
import java.util.List;

public class TianDiTuActivity extends AppCompatActivity {

    private MapView mMapView;
    private TianDiTuLayer vec_c;
    private TianDiTuLayer cva_c;
    private CustomDataSource dataSource;
    private LocationManager locationManager;
    private LocationDisplay mLocationDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tianditu);
        int[] tdtLayers = getIntent().getIntArrayExtra("TIANDITU_LAYERS");
        if ((tdtLayers == null) || (tdtLayers.length == 0)) {
            tdtLayers = new int[]{0, 1};
        }

        mMapView = findViewById(R.id.mapView);
        //去除水印
        ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud4449636536,none,NKMFA0PL4S0DRJE15166");
        //去除logo
        mMapView.setAttributionTextVisible(false);

        ArcGISMap map = new ArcGISMap();

        vec_c = new TianDiTuLayerBuilder().setLayerType(tdtLayers[0]).build();
        cva_c = new TianDiTuLayerBuilder().setLayerType(tdtLayers[1]).build();

        map.getBasemap().getBaseLayers().add(vec_c);
        map.getBasemap().getBaseLayers().add(cva_c);

        mMapView.setMap(map);

        mLocationDisplay = mMapView.getLocationDisplay();
        //dataSource = new CustomDataSource();
        //mLocationDisplay.setLocationDataSource(dataSource);
        mLocationDisplay.addDataSourceStatusChangedListener(dataSourceStatusChangedEvent -> {
            if (dataSourceStatusChangedEvent.isStarted() || dataSourceStatusChangedEvent.getError() == null) {
                return;
            }
            int requestPermissionsCode = 2;
            String[] requestPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            if (!(ContextCompat.checkSelfPermission(TianDiTuActivity.this, requestPermissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(TianDiTuActivity.this, requestPermissions[1]) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(TianDiTuActivity.this, requestPermissions, requestPermissionsCode);
            } else {
                String message = String.format("Error in DataSourceStatusChangedListener: %s",
                    dataSourceStatusChangedEvent.getSource().getLocationDataSource().getError().getMessage());
                Toast.makeText(TianDiTuActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
        mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.NAVIGATION);
        mLocationDisplay.startAsync();
        //dataSource.startAsync();
        //initGPS();

        loadMapPkage();
    }

    private void loadMapPkage() {
        String mainMMPKPath = Environment.getExternalStorageDirectory()+"/ArcGIS/SanFrancisco.mmpk";
        File mmpk = new File(mainMMPKPath);
        final MobileMapPackage mainMobileMapPackage = new MobileMapPackage(mainMMPKPath);
        mainMobileMapPackage.loadAsync();
        mainMobileMapPackage.addDoneLoadingListener(() -> {
            LoadStatus mainLoadStatus = mainMobileMapPackage.getLoadStatus();
            if (mainLoadStatus == LoadStatus.LOADED) {
                List<ArcGISMap> mainArcGISMapL = mainMobileMapPackage.getMaps();
                ArcGISMap mainArcGISMapMMPK = mainArcGISMapL.get(0);
                mMapView.setMap(mainArcGISMapMMPK);
            }
        });
    }

    private void initGPS() {
        //获取LocationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        //低精度，如果设置为高精度，依然获取不了location。
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        //要求海拔
        criteria.setAltitudeRequired(true);
        //要求方位
        criteria.setBearingRequired(true);
        //允许有花费
        criteria.setCostAllowed(true);
        //低功耗
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = null;
        // 获取所有可用的位置提供器
        List<String> providerList = locationManager.getProviders(true);
        if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            // 获取最好的定位方式,true 代表从打开的设备中查找
            provider = locationManager.getBestProvider(criteria, true);
        }
        // 当没有可用的位置提供器时，弹出Toast提示用户
        if (provider == null || provider.trim().length() == 0) {
            Toast.makeText(this, "请打开您的GPS或定位服务", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        broadcast(location);
        locationManager.requestLocationUpdates(provider, 1000, 1, locationListener);
    }

    private void broadcast(Location location) {
        if (location != null && dataSource != null) {
            LocationDataSource.Location agLocation = new LocationDataSource.Location(
                new Point(location.getLongitude(), location.getLatitude(), SpatialReference.create(4326))
            );
            dataSource.changeLocation(agLocation);
        }
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
        mMapView.dispose();
        super.onDestroy();
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onLocationChanged(Location location) {
            // 更新当前设备的位置信息
            broadcast(location);
        }
    };

}
