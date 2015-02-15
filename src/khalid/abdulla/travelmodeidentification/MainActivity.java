package khalid.abdulla.travelmodeidentification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import khalid.abdulla.travelmodeidentification.WriteSensorData;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MainActivity extends ActionBarActivity {

	// GPS Sensor manager
	// TODO: Why does this need to be declared here?
	public LocationManager locationManager = null; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);

		//if you want to lock screen for always Portrait mode    
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Check if a travel mode has previously been selected:
		File file = getBaseContext().getFileStreamPath("currentMode.txt");
		if(file.exists()) {
			// If so check which mode is selected and display that:
			String stringOut = readFromFile();

			if (stringOut.equals("walking")) {
				radioGroup.check(R.id.radio_walking);

			} else if (stringOut.equals("cycling")) {
				radioGroup.check(R.id.radio_cycling);

			} else if (stringOut.equals("tram")) {
				radioGroup.check(R.id.radio_tram);

			} else if (stringOut.equals("train")) {
				radioGroup.check(R.id.radio_train);

			} else if (stringOut.equals("bus")) {
				radioGroup.check(R.id.radio_bus);

			} else if (stringOut.equals("car_passenger")) {
				radioGroup.check(R.id.radio_car_passenger);

			} else if (stringOut.equals("car_driver")) {
				radioGroup.check(R.id.radio_car_driver);

			} else if (stringOut.equals("waiting_mode")) {
				radioGroup.check(R.id.radio_waiting_mode);

			} else if (stringOut.equals("not_travelling")) {
				radioGroup.check(R.id.radio_not_travelling);
			}

		} else {
			// If file doesn't exit (first run of app); then select not_travelling			
			radioGroup.check(R.id.radio_not_travelling);

			// and write that as current status
			writeToFile("not_travelling");
		}
		
	}

	/*----Method to Check GPS is enabled or disabled, and display warning ----- */  
	private void checkGpsStatus() {  
		boolean gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);  
		if (!gpsStatus) {  
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle("GPS is Off!")
			.setMessage("The GPS is off, please turn it on so we can collect location data!")
			.setNegativeButton("Ok", null)
			.show();
		}
	}

	// Routine to run when App is paused (e.g. user navigates to another App)
	protected void onPause() {
		super.onPause();
	}

	// Routine to run when App is resumed (make sure correct radio button selected))
	protected void onResume() {
		super.onResume();
		/* Comment this out for now - don't think it's needed
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
		File file = getBaseContext().getFileStreamPath("currentMode.txt");
		if(!file.exists()) {
			// If not mode selected default to not_travelling:
			radioGroup.check(R.id.radio_not_travelling);

			// and write that as current status
			writeToFile("not_travelling");
			return;
		}

		// Otherwise make sure correct button is set
		String stringOut = readFromFile();

		if (stringOut.equals("walking"))
			radioGroup.check(R.id.radio_walking);

		else if (stringOut.equals("cycling"))
			radioGroup.check(R.id.radio_cycling);

		else if (stringOut.equals("tram"))
			radioGroup.check(R.id.radio_tram);

		else if (stringOut.equals("train"))
			radioGroup.check(R.id.radio_train);

		else if (stringOut.equals("bus"))
			radioGroup.check(R.id.radio_bus);

		else if (stringOut.equals("car_passenger"))
			radioGroup.check(R.id.radio_car_passenger);

		else if (stringOut.equals("car_driver"))
			radioGroup.check(R.id.radio_car_driver);

		else if (stringOut.equals("waiting_mode"))
			radioGroup.check(R.id.radio_waiting_mode);

		else if (stringOut.equals("not_travelling"))
			radioGroup.check(R.id.radio_not_travelling);

		 */
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// GPS uses location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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

	// Handle user selecting a radio button option
	public void onRadioButtonClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

		// Check which radio button was clicked, set string, and register or unregister listeners
		switch(view.getId()) {
		case R.id.radio_walking:
			if (checked){
				writeToFile("walking");
				checkGpsStatus();
				startNewService(view);
			}
			break;
		case R.id.radio_cycling:
			if (checked){
				writeToFile("cycling");
				checkGpsStatus();
				startNewService(view);
			}
			break;
		case R.id.radio_tram:
			if (checked){
				writeToFile("tram");
				checkGpsStatus();
				startNewService(view);
			}
			break;
		case R.id.radio_train:
			if (checked){
				writeToFile("train");
				checkGpsStatus();
				startNewService(view);
			}
			break;
		case R.id.radio_bus:
			if (checked){
				writeToFile("bus");
				checkGpsStatus();
				startNewService(view);
			}
			break;
		case R.id.radio_car_passenger:
			if (checked){
				writeToFile("car_passenger");
				checkGpsStatus();
				startNewService(view);
			}
			break;
		case R.id.radio_car_driver:
			if (checked){
				writeToFile("car_driver");
				checkGpsStatus();
				startNewService(view);
			}
			break;
		case R.id.radio_waiting_mode:
			if (checked){
				writeToFile("waiting_mode");
				checkGpsStatus();
				startNewService(view);
			}
			break;
		case R.id.radio_not_travelling:
			if (checked) {
				writeToFile("not_travelling");
				writeNotTravelling();
				stopNewService(view);
			}
			break;
		}
	}

	// Handle user clicking button to upload new data
	public void sendMessage(View view) {
		// Search for any new files, which pre-date today & have not been marked as sent
		// Look for up to nDaysCheck ago; and reset counter if an unsent file is found

		// Cycle through previous nDaysCheck looking for unsent files
		int nDaysCheck = 10;

		// List in which to store all files to send
		ArrayList<File> filesToSend = new ArrayList<File>();

		for(int daysAgo=1; daysAgo<=nDaysCheck; daysAgo++){
			Calendar c1 = Calendar.getInstance();
			c1.add(Calendar.DAY_OF_YEAR,  -daysAgo);
			// Get value's for this previous date
			int day_of_month = c1.get(Calendar.DAY_OF_MONTH);
			int month_num = (c1.get(Calendar.MONTH)+1); // NB: months are zero-indexed!
			int year_num = c1.get(Calendar.YEAR);

			// Generate candidate fileName
			String dateString = year_num + "_" + month_num + "_" + day_of_month;
			String userString = getUsername();
			File outFile = new File(((Context)this).getExternalFilesDir(null), dateString + "_" + userString + "_TranspModeLog.txt");

			if (!outFile.exists())
			{
				// This file doesn't exist; do nothing look at day before
			} else {
				// This file does exist: process it and extend search further back in time
				nDaysCheck = nDaysCheck + daysAgo;

				// Rename the file to mark it as sent (NB: this is done before actually sending
				// so NOT ROBUST to the user canceling TODO: try to sort this out.
				File sentOutFile = new File(((Context)this).getExternalFilesDir(null), dateString + "_" + userString + "_TranspModeLog_SENT.txt");
				outFile.renameTo(sentOutFile);

				// Add file to list to be sent off
				filesToSend.add(sentOutFile);
			}
		}

		// If there are new files to send, then send them
		if(filesToSend.size() > 0) {

			Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			emailIntent.setType("message/rfc822");
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "TMI Log Files");
			String aEmailList[] = { "ibm.tmi.inbox@gmail.com" };
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);

			//convert from Files to Android friendly Parcelable Uri's
			ArrayList<Uri> filesToSend_Uri = new ArrayList<Uri>();
			for (File file : filesToSend)
			{
				Uri u = Uri.fromFile(file);
				filesToSend_Uri.add(u);
			}
			emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, filesToSend_Uri);
			startActivity(Intent.createChooser(emailIntent, "Send e-mail using..."));

			// Otherwise inform user there are no new files
		} else {
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle("No New Data!")
			.setMessage("No new data to send; please try again when you've been using the App for longer. Only full days of data are sent")
			.setNegativeButton("Ok", null)
			.show();
		}
	}

	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	/* Extract user-name; used in file-naming */
	public String getUsername(){
		AccountManager manager = AccountManager.get(this); 
		Account[] accounts = manager.getAccountsByType("com.google"); 
		List<String> possibleEmails = new LinkedList<String>();

		for (Account account : accounts) {
			// TODO: Check possibleEmail against an email regex or treat
			// account.name as an email address only for certain account.type values.
			possibleEmails.add(account.name);
		}

		if(!possibleEmails.isEmpty() && possibleEmails.get(0) != null){
			String email = possibleEmails.get(0);
			String[] parts = email.split("@");
			if(parts.length > 0 && parts[0] != null)
				return parts[0];
			else
				return null;
		}else
			return null;
	}

	// Write string to file (current "mode")
	private void writeToFile(String data) {
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("currentMode.txt", Context.MODE_PRIVATE));
			outputStreamWriter.write(data);
			outputStreamWriter.close();
		}
		catch (IOException e) {
			Log.e("Exception", "File write failed: " + e.toString());
		} 
	}

	// Read string from file (current "mode"):
	private String readFromFile() {
		// Check if file exists yet, if not return null
		File file = getBaseContext().getFileStreamPath("currentMode.txt");
		if(!file.exists()) {
			return null;
		}
		String ret = "";
		try {
			InputStream inputStream = openFileInput("currentMode.txt");
			if ( inputStream != null ) {
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				String receiveString = "";
				StringBuilder stringBuilder = new StringBuilder();
				while ( (receiveString = bufferedReader.readLine()) != null ) {
					stringBuilder.append(receiveString);
				}
				inputStream.close();
				ret = stringBuilder.toString();
			}
		}
		catch (FileNotFoundException e) {
			Log.e("login activity", "File not found: " + e.toString());
		} catch (IOException e) {
			Log.e("login activity", "Can not read file: " + e.toString());
		}
		return ret;
	}

	// Start the  service
	public void startNewService(View view) {
		Intent serviceIntent = new Intent(this, WriteSensorData.class);
		serviceIntent.putExtra("currentMode", readFromFile());
		serviceIntent.putExtra("userName", getUsername());
		startService(serviceIntent);
	}

	// Stop the  service
	public void stopNewService(View view) {
		stopService(new Intent(this, WriteSensorData.class));
	}

	// Write row out to confirm "not_travelling" mode selected
	private void writeNotTravelling() {

		try {
			Calendar c = Calendar.getInstance(); 
			int day_of_month = c.get(Calendar.DAY_OF_MONTH);
			int month_num = (c.get(Calendar.MONTH)+1);	// NB: months are zero-indexed
			int year_num = c.get(Calendar.YEAR);
			String dateString = year_num + "_" + month_num + "_" + day_of_month;
			String userString = getUsername();

			File outFile = new File(((Context)this).getExternalFilesDir(null), dateString + "_" + userString + "_TranspModeLog.txt");
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
			writer.write(currentDateTimeString + ", " + readFromFile() + ", null, null, null, null, " +
					"null, null, null, null, null, null, null, null, null, null, null, null, null, " +
					"null, null, null, null, null, null, null, null, null \n");
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

	// If user selects to exit App and we are not in "not_travelling" mode
	// then check if they are sure
	@Override
	public void onBackPressed() {
		if(!readFromFile().equals("not_travelling")) {
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle("Still logging!")
			.setMessage("Closing the TMI App will stop logging! \n\n Use the home button " +
					"to navigate away whilst leaving TMI logging in the background. Otherwise select 'Not currently travelling' to log " +
					"that you are no longer on the move, before exiting the App.")
					//					.setPositiveButton("Yes", new DialogInterface.OnClickListener()
					//					{
					//						@Override
					//						public void onClick(DialogInterface dialog, int which) {
					//							// TODO: Should terminate the logging service if they proceed to move away
					//							finish();    
					//						}
					//
					//					})
					.setNegativeButton("Ok", null)
					.show();
		} else {
			// TODO: Should terminate the logging service if they proceed to move away
			finish();   
		}
	}
}