package com.wshunli.map.tianditu.sample;

import com.esri.arcgisruntime.location.LocationDataSource;

public class CustomDataSource extends LocationDataSource {
    @Override
    protected void onStart() {
        onStartCompleted(null);
    }

    @Override
    protected void onStop() {

    }

    public void changeLocation(LocationDataSource.Location location) {
        this.updateLocation(location);
    }
}
