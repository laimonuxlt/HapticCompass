//the  HapticCompass program used examples code adopted from android documentation
// website  https://developer.android.com/docs

package com.example.hapticcompass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEventVisibility;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String BELT_ID = "BELT_ID";
    private static final String TAG = MainActivity.class.getSimpleName();

    TextView txt_azimuth;
    private int azimuth = 0;
    private SensorManager sensorManager;
    private Sensor rotation;
    float[] rotationMatrix = new float[9];
    float[] orientation = new float[9];

    private float currentDegree = 0f;
    private ParticleDevice mDevice;
    //private int result;
    private String n;
    private String funcName = "north";
    private Runnable runnable;
    private int interval = 1000;
    private long lastTime = 0;
    private SensorEvent event;


    @Override
    //method executed at the start of the App
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialise the Particle Cloud SDK
        ParticleCloudSDK.init(this);

        //set listener for connect button to call login method fot logging into the Particle cloud
        findViewById(R.id.connect_btn).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                login();

            }
        });

        //initialise the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //ensure that sensor is presented on the phone and create the instance of SensorManager
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) {
            rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            sensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(getApplicationContext(), "Your phone is missing sensor", Toast.LENGTH_LONG).show();
        }

        //take the azimuth value from Text view and show it
        txt_azimuth = (TextView) findViewById(R.id.txt_azimuth);

        sendFunction();


    }


    public void noSensorAlert() {
    }

    //method to log into the Particle cloud
    public void login() {

        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {


            @Override
            public Object callApi(@NonNull ParticleCloud sparkCloud) throws ParticleCloudException, IOException {
                sparkCloud.logIn("XXXXXXXXX@student.gla.ac.uk", "XXXXXXXX"); //hardcoded email address and password
                mDevice = sparkCloud.getDevice("2XXXXXXXXXXXXXXXXXXXXXX1"); // tha Haptic Belt controller ID


                return -1;
            }

            @Override
            public void onSuccess(@NonNull Object value) {
                Toaster.l(MainActivity.this, "Logged in");
                if (mDevice.isConnected()) {
                    Toaster.l(MainActivity.this, mDevice.toString());
                }
            }

            @Override
            public void onFailure(ParticleCloudException exception) {
                Toaster.s(MainActivity.this, "Something went wrong");
            }
        });
    }

    //method to resume after the activity paused to  saves battery life
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //method to disable sensors after the activity paused. Saves battery life
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, rotation);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager = null;
        rotation = null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //The best option is to use Synthetic type Rotation Vector sensor.
        // Underlying sensors: Accelerometer, Gyroscope and Magnetometer.
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            //the rotation matrix calculations
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);


            azimuth = (int) ((Math.toDegrees(SensorManager.getOrientation(rotationMatrix, orientation)[0]) + 360) % 360);
            txt_azimuth.setText("Azimuth : " + Integer.toString(azimuth) + "° " + n);


            if (azimuth >= 337 && azimuth <= 360 || azimuth >= 0 && azimuth <= 22) {
                n = "N"; // 0 deg

            } else if (azimuth > 22 && azimuth < 68) {
                n = "NE"; //45 deg

            } else if (azimuth >= 68 && azimuth <= 112) {
                n = "E"; // 90deg

            } else if (azimuth > 112 && azimuth < 157) {
                n = "SE"; //135deg

            } else if (azimuth >= 157 && azimuth <= 203) {
                n = "S"; //180deg

            } else if (azimuth > 203 && azimuth < 248) {
                n = "SW"; //225deg

            } else if (azimuth >= 248 && azimuth <= 293) {
                n = "W"; //270deg

            } else if (azimuth > 293 && azimuth < 337) {
                n = "NW";//315deg

            }
        }
    }

    public void sendFunction() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                try {
                    ParticleCloudSDK.getCloud().publishEvent("north", n, ParticleEventVisibility.PUBLIC, 60);
                } catch (ParticleCloudException e) {
                    e.printStackTrace();
                }
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 1, 1000);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}



