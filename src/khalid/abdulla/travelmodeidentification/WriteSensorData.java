package khalid.abdulla.travelmodeidentification;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

// WriteSensorData service - runs in back-ground, initiated from main App
// when a logging mode is selected.

public class WriteSensorData extends Service implements SensorEventListener {

	// Objects to be used in-between other methods:
	// Set-up for sensor readings
	private SensorManager senSensorManager = null;

	// and for GPS
	private LocationManager locationManager = null;  
	private LocationListener locationListener = null;

	// and for Battery Readings
	private Intent batteryStatus;

	// variables passed from the app:
	private String userName = null;
	private String currentMode = null;

	public WriteSensorData() {
	}

	// TODO: Need to read in userName and currentMode from main App

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}


	@Override
	public void onCreate() {
		// Initialise the sensor managers
		senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		// GPS uses location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		batteryStatus = this.registerReceiver(null, ifilter);

		// Set-up listeners:
		// TODO: Check if there is a more efficient way to do this!
		senSensorManager.registerListener(this, senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		senSensorManager.registerListener(this, senSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);
		senSensorManager.registerListener(this, senSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
		senSensorManager.registerListener(this, senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
		senSensorManager.registerListener(this, senSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
		senSensorManager.registerListener(this, senSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
		senSensorManager.registerListener(this, senSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);

		// GPS uses location listener
		locationListener = new MyLocationListener();
		
		// NB: set minm time between location updates and minimum distance here
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, sampleTimeMillis, minGPSdistance, locationListener);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// For time consuming and long tasks you can launch a new thread here...
		userName = intent.getStringExtra("userName");
		currentMode = intent.getStringExtra("currentMode");
		Toast.makeText(this, "Logging Started: " + currentMode, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onDestroy() {
		// Unregister all listeners
		senSensorManager.unregisterListener(this);
		
		// TODO: NB: need to stop listening for location & battery listening
		
		// locationManager.removeUpdates(locationListener);
		// this.unregisterReceiver(null);
		Toast.makeText(this, "Logging Stopped", Toast.LENGTH_LONG).show();
	}

	// Values stored in-between calls to onSensorChanged:
	// strings to store current values of sensor readings:
	private long lastUpdate = 0;
	private long sampleTimeMillis = 500;		// i.e. 0.5 second
	private float minGPSdistance = 5;			// m
	private String x_acc, y_acc, z_acc;
	private String x_grav, y_grav, z_grav;
	private String x_lin_acc, y_lin_acc, z_lin_acc;
	private String x_gyro, y_gyro, z_gyro;
	private String light, x_mag, y_mag, z_mag;
	private String x_orient, y_orient, z_orient;
	private String prox;
	private String sound_lvl;					// currently unused but kept in-case used in future
	private float[] mGravity, mGeomagnetic;		// arrays used in intermediate calculations
	private String scrn_status;
	private String GPS_lat, GPS_lon;
	private String batt_lvl, batt_volt;

	
	// When sensor values changed; write new row out to text file
	// TODO: NB: I have suppressed deprecation here - would be better to work out correct method!
	@SuppressWarnings("deprecation")
	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		// If not_travelling was last selected mode exit now

		// If no mode has yet been selected then exit now
		File file = getBaseContext().getFileStreamPath("currentMode.txt");
		if(!file.exists()) {
			return;
		}

		Sensor mySensor = sensorEvent.sensor;

		// Every time an event occurs; update the values for that type of sensor
		if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			x_acc = Float.toString(sensorEvent.values[0]);
			y_acc = Float.toString(sensorEvent.values[1]);
			z_acc = Float.toString(sensorEvent.values[2]);
		}
		if (mySensor.getType() == Sensor.TYPE_GRAVITY) {
			x_grav = Float.toString(sensorEvent.values[0]);
			y_grav = Float.toString(sensorEvent.values[1]);
			z_grav = Float.toString(sensorEvent.values[2]);
			mGravity = sensorEvent.values; // For computing orientation (azimuth/pitch/roll)
		}

		if (mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			x_lin_acc = Float.toString(sensorEvent.values[0]);
			y_lin_acc = Float.toString(sensorEvent.values[1]);
			z_lin_acc = Float.toString(sensorEvent.values[2]);
		}

		if (mySensor.getType() == Sensor.TYPE_GYROSCOPE) {
			x_gyro = Float.toString(sensorEvent.values[0]);
			y_gyro = Float.toString(sensorEvent.values[1]);
			z_gyro = Float.toString(sensorEvent.values[2]);
		}

		if (mySensor.getType() == Sensor.TYPE_LIGHT) {
			light = Float.toString(sensorEvent.values[0]);
		}

		if (mySensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			x_mag = Float.toString(sensorEvent.values[0]);
			y_mag = Float.toString(sensorEvent.values[1]);
			z_mag = Float.toString(sensorEvent.values[2]);
			mGeomagnetic = sensorEvent.values; // Used in computing orientation (azimuth/pitch/roll)
		}

		if (mySensor.getType() == Sensor.TYPE_PROXIMITY) {
			prox = Float.toString(sensorEvent.values[0]);
		}

		long curTime = System.currentTimeMillis();

		// Only record new values if required interval has passed
		// and last selected mode was NOT not_travelling

		if ((curTime - lastUpdate) > sampleTimeMillis) {
			lastUpdate = curTime;

			// Calculate updated azimuth/pitch/roll values
			if (mGravity != null && mGeomagnetic != null) {
				float R[] = new float[9];
				float I[] = new float[9];
				boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
				if (success) {
					float orientation[] = new float[3];
					SensorManager.getOrientation(R, orientation);
					x_orient = Float.toString(orientation[0]); // orientation contains: azimuth, pitch and roll
					y_orient = Float.toString(orientation[1]); 
					z_orient = Float.toString(orientation[2]); 
				}
			}

			// Get battery values

			// NB: re-register battery listener: this shouldn't be necessary but prevoiusly value only updated
			// when mode changed (and so listener re-registered):
			// TODO: Attempt to resolve this as it seems messy!
			
			IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
			batteryStatus = this.registerReceiver(null, ifilter);

			int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);

			float batteryPct = (100*(float)level) / (float)scale;
			batt_lvl = Float.toString(batteryPct);
			batt_volt = Float.toString((float)voltage);

			// Check if screen is on:
			PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);

			// TODO: this method is deprecated ideally check level of API, and use appropriate method
			// (API20 or more):
			if (powerManager.isScreenOn()){ scrn_status = "On"; }
			else {scrn_status = "Off";}

			// Write new row to data-file
			writeNewRow();

		}
	}

	// Not used but needs to be implemented
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	// Write a new row of data to the output file
	private void writeNewRow() {
		// Write latest sensor values to file			
		try {
			Calendar c = Calendar.getInstance(); 
			int day_of_month = c.get(Calendar.DAY_OF_MONTH);
			int month_num = (c.get(Calendar.MONTH)+1);	// NB: months are zero-indexed!
			int year_num = c.get(Calendar.YEAR);
			String dateString = year_num + "_" + month_num + "_" + day_of_month;

			File outFile = new File(((Context)this).getExternalFilesDir(null), dateString + "_" + userName + "_TranspModeLog.txt");
			if (!outFile.exists())
			{
				// Create file and add in a header
				outFile.createNewFile();
				BufferedWriter writer = new BufferedWriter(new FileWriter(outFile, true /*append*/));
				writer.write("date_and_time, travel_mode, x_acc [m/s^2], y_acc [m/s^2], z_acc [m/s^2],"
						+ " x_grav [m/s^2], y_grav [m/s^2], z_grav [m/s^2], x_lin_acc [m/s^2],"
						+ " y_lin_acc [m/s^2], z_lin_acc [m/s^2], x_gyro [rad/s], y_gyro [rad/s],"
						+ " z_gyro [rad/s], light [lux], x_mag [uT], y_mag [uT], z_mag [uT],"
						+ " x_orient [rad], y_orient [rad], z_orient [rad], prox [cm], sound_lvl [db], scrn_status," +
						" GPS_lat, GPS_lon, batt_lvl [%], batt_volt [mV] \n");
				writer.close();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(outFile, true /*append*/));
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd HH:mm:ss.SSS", Locale.US);
			Date now = new Date();
			String currentDateTimeString = dateFormat.format(now);
			writer.write(currentDateTimeString + ", " + currentMode + ", " + x_acc + ", " + y_acc + ", " + z_acc + ", "
					+ x_grav + ", " + y_grav + ", " + z_grav + ", " + x_lin_acc + ", " + y_lin_acc + ", " + z_lin_acc + ", "
					+ x_gyro + ", " + y_gyro + ", " + z_gyro + ", " + light + ", " + x_mag + ", " + y_mag + ", " + z_mag + ", "
					+ x_orient + ", " + y_orient + ", " + z_orient + ", " + prox + ", " + sound_lvl + ", " + scrn_status + ", "
					+ GPS_lat + ", " + GPS_lon + ", " + batt_lvl + ", " + batt_volt + " \n");
			writer.close();

			// Refresh the data so it can seen when the device is plugged in a
			// computer. You may have to unplug and replug the device to see the 
			// latest changes. This is not necessary if the user should not modify
			// the files.
			MediaScannerConnection.scanFile((Context)(this),
					new String[] { outFile.toString() }, null, null);
		} 
		catch (IOException e) 
		{
			Log.e("khalid.abdulla.travelmodeidentification.FileTest", "Unable to write to the Output File.");
		}
	}

	/*----------Listener class to get coordinates ------------- */  
	private class MyLocationListener implements LocationListener {  
		@Override  
		public void onLocationChanged(Location loc) {  
			GPS_lat = Double.toString(loc.getLatitude());
			GPS_lon = Double.toString(loc.getLongitude());
		}  

		@Override  
		public void onProviderDisabled(String provider) {  
			// TODO Auto-generated method stub           
		}  

		@Override  
		public void onProviderEnabled(String provider) {  
			// TODO Auto-generated method stub           
		}  

		@Override  
		public void onStatusChanged(String provider,   
				int status, Bundle extras) {  
			// TODO Auto-generated method stub           
		}  
	}

}
