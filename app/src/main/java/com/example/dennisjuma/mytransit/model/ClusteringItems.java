package com.example.dennisjuma.mytransit.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusteringItems implements ClusterItem {

    private final LatLng mPosition;

    public ClusteringItems (double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }


    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }
}