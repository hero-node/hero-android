/**
 * BSD License
 * Copyright (c) Hero software.
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.

 * Neither the name Hero nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific
 * prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.hero;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.hero.depandency.LocationUtils;
import com.hero.depandency.MPermissionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class HeroLocationView extends FrameLayout implements IHero {
    private static final String TAG = "HeroLocationView";

    private final static int LOCATION_TIMEOUT = 20 * 1000;
    private LocationManager locationManager;
    private boolean gpsEnabled = false;
    private boolean networkEnabled = false;
    private LocationResult locationResult;
    private Timer timer;
    private int visibility;
    protected BaiduMap baiduMap;
    protected MapView mapView;
    protected BitmapDescriptor currentMarker;
    private final boolean useBaiduLBS = true;
    protected float zoomLevel = 5f;
    protected LatLng currentMapLoc;

    DialogInterface.OnClickListener openLocationListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            if (i == DialogInterface.BUTTON_POSITIVE) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getContext().startActivity(intent);
            }
            dialogInterface.dismiss();
        }
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.i(TAG, "locationListenerNetwork got " + location);
            timer.cancel();
            locationManager.removeUpdates(this);
            locationManager.removeUpdates(locationListenerGps);
            locationResult.gotLocation(location);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.i(TAG, "locationListenerGps got " + location);
            timer.cancel();
            locationManager.removeUpdates(this);
            locationManager.removeUpdates(locationListenerNetwork);
            locationResult.gotLocation(location);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    public HeroLocationView(Context context) {
        super(context);
        visibility = INVISIBLE;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visible) {
        super.onVisibilityChanged(changedView, visibility);
        // view resume to front
        //        if (visible == VISIBLE) {
        //            if (!networkEnabled && !gpsEnabled) {
        //                updateLocationStatus();
        //                requestCurrentLocation(locationResult);
        //            }
        //        }
        visibility = visible;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        release();
    }

    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);
        if (jsonObject.has("coordinate")) {
            setVisibility(VISIBLE);
            createMapView();
            JSONObject coordinate = jsonObject.getJSONObject("coordinate");
            if (coordinate.has("la") && coordinate.has("lo")) {
                createMaker();
                locateMapTo(coordinate.optDouble("la"), coordinate.optDouble("lo"));
            }
        }

        if (jsonObject.has("fetch_coordinate")) {
            Log.i(TAG, "HeroLocationView on ");
            this.setVisibility(GONE);

            if (useBaiduLBS) {
                if (GPSPermissionCheck(jsonObject)) {
                    startBDLocation(getContext(), jsonObject);
                }
            } else {
                if (locationManager == null) {
                    locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                }
                if (locationManager != null) {
                    try {
                        gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (!networkEnabled && !gpsEnabled) {
                        //                    buildOpenLocationDialog();
                    }

                    locationResult = new LocationResult(getContext(), jsonObject);
                    Location loc = getLastKnownLocation();
                    if (loc != null) {
                        locationResult.gotLocation(loc);
                        return;
                    }

                    if (!requestCurrentLocation(locationResult)) {
                        if (getContext() != null) {
                            JSONObject fetch_coordinate = null;
                            try {
                                fetch_coordinate = jsonObject.getJSONObject("fetch_coordinate");
                                fetch_coordinate.put("err", -1);
                                fetch_coordinate.put("location", "err");
                                ((IHeroContext) getContext()).on(fetch_coordinate);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    public Location getLastKnownLocation() {
        Location netLoc = null;
        Location gpsLoc = null;
        if (gpsEnabled) {
            gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (networkEnabled) {
            netLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        // If there are both values use the latest one
        if (gpsLoc != null && netLoc != null) {
            if (gpsLoc.getTime() > netLoc.getTime()) {
                return gpsLoc;
            } else {
                return netLoc;
            }
        }

        if (gpsLoc != null) {
            return gpsLoc;
        }
        if (netLoc != null) {
            return netLoc;
        }
        return null;
    }

    protected void createMapView() {
        if (mapView == null) {
            SDKInitializer.initialize(getContext().getApplicationContext());
            mapView = new MapView(getContext());
            addView(mapView);
            baiduMap = mapView.getMap();
            baiduMap.setMapStatus(MapStatusUpdateFactory.zoomBy(zoomLevel));
        }
    }

    protected void createMaker() {
        if (currentMarker == null) {
            currentMarker = BitmapDescriptorFactory.fromResource(R.drawable.icon_location);
        }
    }

    public void locateMapTo(final double la, final double lo) {
        currentMapLoc = new LatLng(la, lo);
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(currentMapLoc);
        baiduMap.animateMapStatus(update);

        MyLocationData.Builder builder = new MyLocationData.Builder();
        builder.latitude(la);
        builder.longitude(lo);
        MyLocationData data = builder.build();
        baiduMap.setMyLocationData(data);
        if (currentMarker == null) {
            createMaker();
        }
        MarkerOptions markOption = new MarkerOptions().position(currentMapLoc).icon(currentMarker).zIndex(9).draggable(false);
        baiduMap.addOverlay(markOption);
        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            public boolean onMarkerClick(final Marker marker) {
                //                baidulo = googlelo + 0.0065 baidula = googlela + 0.0060
                Uri uri = Uri.parse("geo:" + la + "," + lo);
                Intent it = new Intent(Intent.ACTION_VIEW, uri);
                getContext().startActivity(it);
                return true;
            }
        });
    }

    private void updateLocationStatus() {
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean requestCurrentLocation(LocationResult result) {
        Log.i(TAG, "request location GPS: " + gpsEnabled + " network:" + networkEnabled);

        if (!gpsEnabled && !networkEnabled) {
            return false;
        }

        try {
            if (gpsEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, locationListenerGps);
            }
            if (networkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 0, locationListenerNetwork);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        timer = new Timer();
        timer.schedule(new GetLastLocation(), LOCATION_TIMEOUT);
        return true;
    }

    private void stopLocation() {
        Log.i(TAG, "stopLocation");
        if (useBaiduLBS) {
            stopBDLocation();
        } else {
            if (timer != null) {
                timer.cancel();
            }
            if (locationManager != null) {
                locationManager.removeUpdates(locationListenerGps);
                locationManager.removeUpdates(locationListenerNetwork);
            }
        }
    }

    private void buildOpenLocationDialog() {
        new AlertDialog.Builder(getContext()).setTitle(R.string.locationOpenTitle).setMessage(R.string.locationOpenContent).setPositiveButton(R.string.locationOpenPositiveButton, openLocationListener).setNegativeButton(R.string.locationOpenNegativeButton, openLocationListener).create().show();
    }

    class GetLastLocation extends TimerTask {
        @Override
        public void run() {
            locationManager.removeUpdates(locationListenerGps);
            locationManager.removeUpdates(locationListenerNetwork);

            Location loc = getLastKnownLocation();
            locationResult.gotLocation(loc);
        }
    }

    class LocationResult {
        private Context context;
        private JSONObject json;

        LocationResult(Context c, JSONObject js) {
            context = c;
            json = js;
        }

        public void gotLocation(Location location) {
            JSONObject fetch_coordinate = null;
            if (location != null) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                try {
                    fetch_coordinate = json.getJSONObject("fetch_coordinate");
                    fetch_coordinate.put("la", lat);
                    fetch_coordinate.put("lo", lng);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "LocationResult got " + location);
                //                Toast.makeText(getContext(), "LocationResult got " + location, Toast.LENGTH_SHORT).show();
                if (context != null && fetch_coordinate != null) {
                    try {
                        ((IHeroContext) context).on(fetch_coordinate);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    fetch_coordinate = json.getJSONObject("fetch_coordinate");
                    fetch_coordinate.put("err", -1);
                    fetch_coordinate.put("location", "err");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "LocationResult got " + location);
            }
            if (context != null && fetch_coordinate != null) {
                try {
                    ((IHeroContext) context).on(fetch_coordinate);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void startBDLocation(final Context context, final JSONObject json) {
        LocationUtils.getInstance().stopLocation();
        LocationUtils.getInstance().startLocation(context, new LocationUtils.OnLocationListener() {
            @Override
            public void onLocation(BDLocation location) {
                JSONObject fetch_coordinate = null;
                try {
                    fetch_coordinate = json.getJSONObject("fetch_coordinate");
                    if (fetch_coordinate != null) {
                        // sdk could return 4.9e-324
                        if (location == null || !isLocationValueValid(location.getLatitude()) || !isLocationValueValid(location.getLongitude())) {
                            fetch_coordinate.put("err", -1);
                            fetch_coordinate.put("location", "err");
                            Log.i(TAG, "BDLocationResult got failed");
                        } else {
                            fetch_coordinate.put("la", location.getLatitude());
                            fetch_coordinate.put("lo", location.getLongitude());
                            Log.i(TAG, "BDLocationResult got " + location.getLatitude() + ":" + location.getLongitude());
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (context != null && fetch_coordinate != null) {
                    try {
                        ((IHeroContext) context).on(fetch_coordinate);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                stopLocation();
            }
        });
    }

    private boolean isLocationValueValid(double value) {
        if (value != 0.0) {
            if (String.valueOf(value).contains("E") || String.valueOf(value).contains("e")) {
                return false;
            }
            return true;
        }
        return false;
    }

    private void stopBDLocation() {
        LocationUtils.getInstance().stopLocation();
    }

    private void reportLocationFail(JSONObject jsonObject) {
        JSONObject fetch_coordinate = null;
        try {
            fetch_coordinate = jsonObject.getJSONObject("fetch_coordinate");
            fetch_coordinate.put("err", -1);
            fetch_coordinate.put("location", "err");
            ((IHeroContext) getContext()).on(fetch_coordinate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean GPSPermissionCheck(JSONObject jsonObject) {
        if (!MPermissionUtils.checkAndRequestPermissions(getContext(), new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE}, MPermissionUtils.HERO_PERMISSION_LOCATION)) {
            reportLocationFail(jsonObject);
            return false;
        }
        return true;
    }

    private void destroyBaiduMapView() {
        if (baiduMap != null) {
            baiduMap.setMyLocationEnabled(false);
        }
        if (mapView != null) {
            mapView.onDestroy();
            mapView = null;
        }
    }

    protected void release() {
        stopLocation();
        destroyBaiduMapView();
    }
}
