package com.emanderson.virtualdevicemanagerhelper;

import java.util.ArrayList;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.ViewConfiguration;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class VDMHelper extends Activity {
    
    private ListView attributeLV;
    private ArrayList<String> attributes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vdmhelper);
        
        attributes = new ArrayList<String>();
        showModelName();
        showDisplayProperties();
        showSensorPresence();
        showHardwarePresence();
        showMemoryInfo();
        
        attributeLV = (ListView)findViewById(R.id.attributeListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, attributes);
        attributeLV.setAdapter(adapter);
    }

    @SuppressLint("NewApi")
    private void showHardwarePresence() {
        Configuration config = getResources().getConfiguration();
        boolean hasKeyboard = false;
        // NOTE: presumes 12-key keyboard doesn't count
        if (config.keyboard == Configuration.KEYBOARD_QWERTY) {
            hasKeyboard = true;
        }
        Log.i("VDMHelper", String.format("Keyboard: %b", hasKeyboard));
        attributes.add(String.format("Keyboard: %b", hasKeyboard));
        if (config.navigation == Configuration.NAVIGATION_TRACKBALL) {
            Log.i("VDMHelper", "Navigation: Trackball");
            attributes.add("Navigation: Trackball");
        } else if (config.navigation == Configuration.NAVIGATION_DPAD) {
            Log.i("VDMHelper", "Navigation: DPad");
            attributes.add("Navigation: DPad");
        } else {
            Log.i("VDMHelper", "Navigation: No Nav");
            attributes.add("Navigation: No Nav");
        }
        
        // NOTE: Based on assumptions at http://stackoverflow.com/questions/12031923/is-it-possible-to-detect-android-virtual-buttons-programmatically
        boolean hasHardwareButton = false;
        if (android.os.Build.VERSION.SDK_INT <= 10) {
            hasHardwareButton = true;
        } else if (android.os.Build.VERSION.SDK_INT >= 14 && ViewConfiguration.get(this).hasPermanentMenuKey()) {
            hasHardwareButton = true;
        }
        if (hasHardwareButton) {
            Log.i("VDMHelper", "Buttons: Hardware");
            attributes.add("Buttons: Hardware");
        } else {
            Log.i("VDMHelper", "Buttons: Software");
            attributes.add("Buttons: Software");
        }
    }
    
    @SuppressLint("NewApi")
    private void showMemoryInfo() {
        MemoryInfo memoryInfo = new MemoryInfo();
        ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            Log.i("VDMHelper", "RAM: " + humanReadableByteCount(memoryInfo.totalMem, true));
            attributes.add("RAM: " + humanReadableByteCount(memoryInfo.totalMem, true));
        } else {
            Log.i("VDMHelper", "RAM: unknown");
            attributes.add("RAM: unknown");  
        }
    }
    
    // Credit due to: http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java/3758880#3758880
    private static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private void showModelName() {
        String model = android.os.Build.MODEL;
        Log.i("VDMHelper", "Model name: " + model);
        attributes.add("Model: " + model);
    }

    @SuppressLint("NewApi")
    private void showDisplayProperties() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        if (android.os.Build.VERSION.SDK_INT >= 13) {
            display.getRealSize(size);
        } else {
            size.x = display.getWidth();
            size.y = display.getHeight();
        }
        Log.i("VDMHelper", String.format("Resolution: %d x %d", size.x, size.y));
        attributes.add("Resolution: " + size.x + " x " + size.y);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        String densityText = "unknown";
        switch (displayMetrics.densityDpi) {
        case DisplayMetrics.DENSITY_TV: densityText = "tvdpi"; break;
        case DisplayMetrics.DENSITY_LOW: densityText = "ldpi"; break;
        case DisplayMetrics.DENSITY_MEDIUM: densityText = "mdpi"; break;
        case DisplayMetrics.DENSITY_HIGH: densityText = "hdpi"; break;
        case DisplayMetrics.DENSITY_XHIGH: densityText = "xhdpi"; break;
        case DisplayMetrics.DENSITY_XXHIGH: densityText = "xxhdpi"; break;
        }
        Log.i("VDMHelper", "Density: " + densityText);
        attributes.add("Density: " + densityText);
        double widthInches = size.x/displayMetrics.xdpi;
        double heightInches = size.y/displayMetrics.ydpi;
        double screenInches = Math.sqrt(Math.pow(widthInches, 2) + Math.pow(heightInches, 2));
        Log.i("VDMHelper", String.format("Screen Inches: %.02f", screenInches));
        attributes.add(String.format("Screen Size (Aprox.): %.01f\"", screenInches));
        
        Configuration config = getResources().getConfiguration();
        int screenSizeInt = config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        String screenSize = "unknown";
        switch (screenSizeInt) {
        case Configuration.SCREENLAYOUT_SIZE_SMALL: screenSize = "small"; break;
        case Configuration.SCREENLAYOUT_SIZE_NORMAL: screenSize = "normal"; break;
        case Configuration.SCREENLAYOUT_SIZE_LARGE: screenSize = "large"; break;
        case Configuration.SCREENLAYOUT_SIZE_XLARGE: screenSize = "xlarge"; break;
        }
        Log.i("VDMHelper", "Size: " + screenSize);
        attributes.add("Size: " + screenSize);
        
        int screenLengthInt = config.screenLayout & Configuration.SCREENLAYOUT_LONG_MASK;
        String screenRatio = "unknown";
        switch (screenLengthInt) {
        case Configuration.SCREENLAYOUT_LONG_NO: screenRatio = "notlong"; break;
        case Configuration.SCREENLAYOUT_LONG_YES: screenRatio = "long"; break;
        }
        Log.i("VDMHelper", "Screen Ratio: " + screenRatio);
        attributes.add("Screen Ratio: " + screenRatio);
    }
    
    private void showSensorPresence() {
        SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        boolean hasAccelerometer = false;
        boolean hasGyroscope = false;
        boolean hasProximitySensor = false;
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            hasAccelerometer = true;
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            hasGyroscope = true;
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
            hasProximitySensor = true;
        }
        Log.i("VDMHelper", String.format("Accelerometer: %b", hasAccelerometer));
        attributes.add(String.format("Accelerometer: %b", hasAccelerometer));
        Log.i("VDMHelper", String.format("Gyroscope: %b", hasGyroscope));
        attributes.add(String.format("Gyroscope: %b", hasGyroscope));
        Log.i("VDMHelper", String.format("Proximity Sensor: %b", hasProximitySensor));
        attributes.add(String.format("Proximity Sensor: %b", hasProximitySensor));

        boolean hasGPS = false;
        PackageManager packageManager = getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
            hasGPS = true;
        }
        Log.i("VDMHelper", String.format("GPS: %b", hasGPS));
        attributes.add(String.format("GPS: %b", hasGPS));
        
        boolean hasBackCamera = false;
        boolean hasFrontCamera = false;
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            hasBackCamera = true;
        }
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            hasFrontCamera = true;
        }
        Log.i("VDMHelper", String.format("Back Camera: %b", hasBackCamera));
        attributes.add(String.format("Back Camera: %b", hasBackCamera));
        Log.i("VDMHelper", String.format("Front Camera: %b", hasFrontCamera));
        attributes.add(String.format("Front Camera: %b", hasFrontCamera));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.vdmhelper, menu);
        return true;
    }

}
