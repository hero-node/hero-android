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

package com.hero.depandency;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * Created by Haijun.Wang on 16/1/20.
 */
public class LocationUtils {

    private static final int LOOP_LOCATION_INTERVAL = 5 * 60 * 1000;
    private static final int LOCATION_TIMEOUT = 20 * 1000;
    public LocationClient locationClient;
    public MyLocationListener locationListener;
    private static LocationUtils instance;

    public interface OnLocationListener {
        void onLocation(BDLocation location);
    }

    private OnLocationListener onLocationListener;

    public static synchronized LocationUtils getInstance() {
        if (instance == null) {
            instance = new LocationUtils();
        }
        return LocationUtils.instance;
    }

    private LocationUtils() {
        locationListener = new MyLocationListener();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(LOOP_LOCATION_INTERVAL);
        option.setIsNeedAddress(true);
        option.setTimeOut(LOCATION_TIMEOUT);
        locationClient.setLocOption(option);
    }

    public void startLocation(Context context, OnLocationListener listener) {
        if (locationClient == null) {
            locationClient = new LocationClient(context);
            locationClient.registerLocationListener(locationListener);
            initLocation();
            locationClient.start();
        }
        onLocationListener = listener;
    }

    public void stopLocation() {
        if (locationClient != null) {
            locationClient.stop();
            locationClient = null;
        }
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location != null) {
                if (onLocationListener != null) {
                    String locationTime = location.getTime();
                    if (TextUtils.isEmpty(locationTime)) {
                        onLocationListener.onLocation(null);
                        return;
                    }
                    onLocationListener.onLocation(location);
                }
            }
        }
    }
}
