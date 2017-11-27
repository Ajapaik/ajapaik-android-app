package ee.ajapaik.android;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ee.ajapaik.android.BuildConfig;

public class LocationService extends Service implements LocationListener, SensorEventListener {
    private static final String TAG = "LocationService";

    private static final int MAX_AGE_IN_MINUTES = 2;
    private static final int MIN_DISTANCE_IN_METERS = 10;
    private static final int MIN_UPDATE_IN_SECONDS = 30;
    private static final int SENSOR_UPDATE_IN_SECONDS = 5;

    public static void startSettings(Context context) {
        context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    private float[] m_accelerometer = null;
    private float[] m_magneticField = null;
    private final IBinder m_binder = new LocalBinder();
    private List<Listener> m_listeners = new ArrayList<Listener>();
    private boolean m_enabled = false;
    private Location m_location;
    private LocationManager m_manager;

    public LocationService() {
    }

    private void invalidate() {
        boolean hasLocation = m_location != null;

        try {
            SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);

            m_enabled = false;
            m_manager = (LocationManager) getSystemService(LOCATION_SERVICE);

            if (m_manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                m_manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_UPDATE_IN_SECONDS * 1000, MIN_DISTANCE_IN_METERS, this);
                m_enabled = true;

                if(!hasLocation) {
                    m_location = m_manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }

            if(m_manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                m_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_UPDATE_IN_SECONDS * 1000, MIN_DISTANCE_IN_METERS, this);
                m_enabled = true;

                if(!hasLocation) {
                    Location location = m_manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if(location != null) {
                        m_location = location;
                    }
                }
            }

            sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SENSOR_UPDATE_IN_SECONDS * 1000);
            sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SENSOR_UPDATE_IN_SECONDS * 1000);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        if(!hasLocation && m_location != null) {
            synchronized(m_listeners) {
                for(Listener listener : m_listeners) {
                    listener.onLocationChanged(m_location);
                }
            }
        }
    }

    @Override
    public void onCreate() {
        if(BuildConfig.DEBUG) {
            Log.d(TAG, "onCreate");
        }

        super.onCreate();
        invalidate();
    }

    @Override
    public void onDestroy() {
        if(BuildConfig.DEBUG) {
            Log.d(TAG, "onDestroy");
        }

        if(m_manager != null) {
            try {
                m_manager.removeUpdates(this);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return m_binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(m_location != null &&
           Math.abs(location.getTime() - m_location.getTime()) < MAX_AGE_IN_MINUTES * 1000 * 60 &&
           1.5F * Math.max(m_location.getAccuracy(), 1.0F) < location.getAccuracy()) {
            return;
        }

        m_location = location;

        synchronized(m_listeners) {
            for(Listener listener : m_listeners) {
                listener.onLocationChanged(location);
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        invalidate();
    }

    @Override
    public void onProviderEnabled(String provider) {
        invalidate();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                m_magneticField = event.values;
                break;
            case Sensor.TYPE_ACCELEROMETER:
                m_accelerometer = event.values;
                break;
        }

        if(m_magneticField != null && m_accelerometer != null) {
            synchronized(m_listeners) {
                for(Listener listener : m_listeners) {
                    listener.onOrientationChanged();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public static abstract class Connection implements ServiceConnection {
        private LocalBinder m_binder;
        private final Listener m_listener = new Listener() {
            @Override
            public void onLocationChanged(Location location) {
                Connection.this.onLocationChanged(location);
            }

            @Override
            public void onOrientationChanged() {
                Connection.this.onOrientationChanged();
            }
        };
        private boolean m_connecting = false;

        public void connect(Context context) {
            if(!m_connecting) {
                m_connecting = true;
                context.bindService(new Intent(context, LocationService.class), this, Context.BIND_AUTO_CREATE);
            }
        }

        public void disconnect(Context context) {
            if(m_connecting) {
                m_connecting = false;
                context.unbindService(this);
            }
        }

        public boolean isEnabled() {
            return (m_binder != null) ? m_binder.isEnabled() : true;
        }

        public Location getLocation() {
            return (m_binder != null) ? m_binder.getLocation() : null;
        }

        public float[] getOrientation() {
            return (m_binder != null) ? m_binder.getOrientation() : null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "onServiceConnected");
            }

            m_binder = (LocalBinder)service;

            if(m_binder != null) {
                Location location = m_binder.getLocation();

                m_binder.addListener(m_listener);

                if(location != null) {
                    onLocationChanged(location);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "onServiceDisconnected");
            }

            if(m_binder != null) {
                m_binder.removeListener(m_listener);
            }

            m_binder = null;
        }

        public abstract void onLocationChanged(Location location);
        public void onOrientationChanged() { }
    }

    protected class LocalBinder extends Binder {
        public boolean isEnabled() {
            return m_enabled;
        }

        public Location getLocation() {
            return m_location;
        }

        public float[] getOrientation() {
            if(m_accelerometer != null && m_magneticField != null) {
                float[] rotation = new float[9];

                if(SensorManager.getRotationMatrix(rotation, null, m_accelerometer, m_magneticField)) {
                    float[] orientation = new float[3];

                    return SensorManager.getOrientation(rotation, orientation);
                }
            }

            return null;
        }

        public void addListener(Listener listener) {
            synchronized(m_listeners) {
                m_listeners.add(listener);
            }
        }

        public void removeListener(Listener listener) {
            synchronized(m_listeners) {
                m_listeners.remove(listener);
            }
        }
    }

    protected interface Listener {
        void onLocationChanged(Location location);
        void onOrientationChanged();
    }
}
