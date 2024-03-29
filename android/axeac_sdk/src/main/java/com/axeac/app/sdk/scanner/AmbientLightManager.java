/*
 * Copyright (C) 2012 ZXing authors
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

package com.axeac.app.sdk.scanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;

import com.axeac.app.sdk.scanner.camera.CameraManager;
import com.axeac.app.sdk.scanner.camera.FrontLightMode;


/**
 * describe:Detects ambient light and switches on the front light when very dark, and off
 *          <br>again when sufficiently light.
 * <br>检测环境光，并在非常暗的情况下打开前灯，并在足够亮时再次关闭。
 * @author axeac
 * @version 1.0.0
 */
public class AmbientLightManager implements SensorEventListener {

    private static final float TOO_DARK_LUX = 45.0f;
    private static final float BRIGHT_ENOUGH_LUX = 450.0f;

    private final Context context;
    private CameraManager cameraManager;

    // describe:Light sensor
    /**
     * 光传感器
     */
    private Sensor lightSensor;

    public AmbientLightManager(Context context) {
        this.context = context;
    }

    public void start(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        if (FrontLightMode.readPref(sharedPrefs) == FrontLightMode.AUTO) {
            SensorManager sensorManager = (SensorManager) context
                    .getSystemService(Context.SENSOR_SERVICE);
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            if (lightSensor != null) {
                sensorManager.registerListener(this, lightSensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    public void stop() {
        if (lightSensor != null) {
            SensorManager sensorManager = (SensorManager) context
                    .getSystemService(Context.SENSOR_SERVICE);
            sensorManager.unregisterListener(this);
            cameraManager = null;
            lightSensor = null;
        }
    }

    // describe:The method will callback when the surrounding environment changed, and then
    //          according to the critical value set whether to open the flash
    /**
     * 该方法会在周围环境改变后回调，然后根据设置好的临界值决定是否打开闪光灯
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float ambientLightLux = sensorEvent.values[0];
        if (cameraManager != null) {
            if (ambientLightLux <= TOO_DARK_LUX) {
                cameraManager.setTorch(true);
            } else if (ambientLightLux >= BRIGHT_ENOUGH_LUX) {
                cameraManager.setTorch(false);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

}
