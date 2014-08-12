package com.example.bill.hue;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.List;
import java.util.Random;

public class MyActivity extends Activity {
    private PHHueSDK phHueSDK;
    private static final int BRIGHTNESS_MAX = 254;
    TextView lightText, availableText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        phHueSDK = PHHueSDK.create();

        // Set the Device Name (name of your app). This will be stored in your bridge whitelist entry.
        phHueSDK.setDeviceName("Huehuehue");
        // Register the PHSDKListener to receive callbacks from the bridge.
        phHueSDK.getNotificationManager().registerSDKListener(phsdklistener);
        doBridgeSearch(); // Search for the philips hue bridge

        lightText = (TextView) findViewById(R.id.light_output);
        availableText = (TextView) findViewById(R.id.sensor_available);
        lightText.setText("LIGHT: ");

        SensorManager mySensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        Sensor LightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if(LightSensor != null){
            availableText.setText("Sensor.TYPE_LIGHT Available");
            mySensorManager.registerListener(
                    LightSensorListener,
                    LightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);

        } else{
            availableText.setText("Sensor.TYPE_LIGHT NOT Available");
        }
    }

    private PHSDKListener phsdklistener = new PHSDKListener() {
        @Override
        public void onAccessPointsFound(List<PHAccessPoint> phAccessPoints) {
            if (phAccessPoints != null && phAccessPoints.size() > 0) {
                phHueSDK.getAccessPointsFound().clear();
                phHueSDK.getAccessPointsFound().add(phAccessPoints.get(0));
            }
        }

        @Override
        public void onCacheUpdated(int i, PHBridge phBridge) {
        }

        @Override
        public void onBridgeConnected(PHBridge phBridge) {
            phHueSDK.setSelectedBridge(phBridge);
            // TODO: enable heartbeat to get light status
            phHueSDK.enableHeartbeat(phBridge, PHHueSDK.HB_INTERVAL);
            phHueSDK.getLastHeartbeat().put(phBridge.getResourceCache().getBridgeConfiguration().getIpAddress(), System.currentTimeMillis());
        }

        @Override
        public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {

        }

        @Override
        public void onError(int i, String s) {

        }

        @Override
        public void onConnectionResumed(PHBridge phBridge) {

        }

        @Override
        public void onConnectionLost(PHAccessPoint phAccessPoint) {
            if (!phHueSDK.getDisconnectedAccessPoint().contains(phAccessPoint)) {
                phHueSDK.getDisconnectedAccessPoint().add(phAccessPoint);
            }
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final SensorEventListener LightSensorListener
            = new SensorEventListener(){

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_LIGHT){
                lightText.setText("LIGHT: " + event.values[0]);
            }
        }

    };

    public void randomLights() {
        PHBridge bridge = phHueSDK.getSelectedBridge();

        List<PHLight> allLights = bridge.getResourceCache().getAllLights();
        Random rand = new Random();

        for (PHLight light : allLights) {
            PHLightState lightState = new PHLightState();
            lightState.setBrightness(rand.nextInt(BRIGHTNESS_MAX));
            // To validate your lightstate is valid (before sending to the bridge) you can use:
            // String validState = lightState.validateState();
            bridge.updateLightState(light, lightState);   // If no bridge response is required then use this simpler form.
        }
    }

    private void doBridgeSearch() {
        PHBridgeSearchManager sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
        // Start the UPNP Searching of local bridges.
        sm.search(true, true);
    }

    @Override
    protected void onDestroy() {
        PHBridge bridge = phHueSDK.getSelectedBridge();
        if (bridge != null) {

            if (phHueSDK.isHeartbeatEnabled(bridge)) {
                phHueSDK.disableHeartbeat(bridge);
            }

            phHueSDK.disconnect(bridge);
            super.onDestroy();
        }
    }
}