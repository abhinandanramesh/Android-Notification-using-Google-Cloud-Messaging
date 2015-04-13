package com.gcmdemoapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.Person.Image;
import com.gcmdemoapp.R;
import com.gcmdemoapp.RegisterActivity;

public class SignInActivity extends Activity implements OnClickListener,
		PlusClient.ConnectionCallbacks, PlusClient.OnConnectionFailedListener {

	private static final int DIALOG_GET_GOOGLE_PLAY_SERVICES = 1;

	private static final int REQUEST_CODE_SIGN_IN = 1;
	private static final int REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES = 2;

	private TextView mSignInStatus;
	public static PlusClient mPlusClient;
	private SignInButton mSignInButton;
	//private View mSignOutButton;
	public static ConnectionResult mConnectionResult;

	SharedPreferences app_preferences_signin;
	SharedPreferences.Editor editor_signin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sign_in_activity);
		
		
		GPSNetworkActivity gps = new GPSNetworkActivity(SignInActivity.this);
		startService(new Intent(SignInActivity.this,PakshiService.class));
		
		app_preferences_signin = PreferenceManager
				.getDefaultSharedPreferences(SignInActivity.this);
		editor_signin = app_preferences_signin.edit();

		mPlusClient = new PlusClient.Builder(this, this, this).build();
		mSignInStatus = (TextView) findViewById(R.id.sign_in_status);
		mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
		mSignInButton.setOnClickListener(this);
		//mSignOutButton = findViewById(R.id.sign_out_button);
		//mSignOutButton.setOnClickListener(this);

		// //////// IMEI Fetching ///////////////
		Boolean checkstatIMEI = app_preferences_signin.getBoolean("check_IMEI",
				false);
		if (!checkstatIMEI) {
			
			TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			if (telephonyManager.getDeviceId() != null) {
				editor_signin.putString("IMEI", telephonyManager.getDeviceId());
				editor_signin.putBoolean("check_IMEI", true);
				editor_signin.commit();
			} else {
				editor_signin.putBoolean("check_IMEI", false);
				editor_signin.commit();
			}
		}

		// ///////////////////////////////////////

		Boolean checkstatsignin = app_preferences_signin.getBoolean(
				"check_signin", false);
		System.out.println("SignIn");
		System.out.println(checkstatsignin);
		System.out.println("SignIn Status");

		if (checkstatsignin) {
			Intent i = new Intent(this, RegisterActivity.class);
			startActivity(i);
			SignInActivity.this.finish();

		}

	}

	@Override
	public void onStart() {
		super.onStart();
		mPlusClient.connect();
	}

	@Override
	public void onStop() {
		mPlusClient.disconnect();
		super.onStop();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.sign_in_button:

			int available = GooglePlayServicesUtil
					.isGooglePlayServicesAvailable(this);
			if (available != ConnectionResult.SUCCESS) {
				showDialog(DIALOG_GET_GOOGLE_PLAY_SERVICES);
				return;
			}

			try {
				mSignInStatus.setText(getString(R.string.signing_in_status));
				mConnectionResult.startResolutionForResult(this,
						REQUEST_CODE_SIGN_IN);
			} catch (IntentSender.SendIntentException e) {
				// Fetch a new result to start.
				mPlusClient.connect();
			}
			break;
		/*case R.id.sign_out_button:
			if (mPlusClient.isConnected()) {
				mPlusClient.clearDefaultAccount();
				mPlusClient.disconnect();
				mPlusClient.connect();
			}
			break;*/

		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id != DIALOG_GET_GOOGLE_PLAY_SERVICES) {
			return super.onCreateDialog(id);
		}

		int available = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (available == ConnectionResult.SUCCESS) {
			return null;
		}
		if (GooglePlayServicesUtil.isUserRecoverableError(available)) {
			return GooglePlayServicesUtil.getErrorDialog(available, this,
					REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES);
		}
		return new AlertDialog.Builder(this)
				.setMessage(R.string.plus_generic_error).setCancelable(true)
				.create();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_SIGN_IN
				|| requestCode == REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES) {
			if (resultCode == RESULT_OK && !mPlusClient.isConnected()
					&& !mPlusClient.isConnecting()) {
				// This time, connect should succeed.
				mPlusClient.connect();
			}
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {

		Boolean checkstatlogout = app_preferences_signin.getBoolean(
				"check_logout", false);
		if (!checkstatlogout) {

			String currentPersonName = mPlusClient.getCurrentPerson() != null ? mPlusClient
					.getCurrentPerson().getDisplayName()
					: getString(R.string.unknown_person);
			mSignInStatus.setText(getString(R.string.signed_in_status,
					currentPersonName));
			updateButtons(true /* isSignedIn */);

			if (mPlusClient.getCurrentPerson() != null) {


				Person currentPerson = mPlusClient.getCurrentPerson();
				String name = currentPerson.getDisplayName();
				String emailid = mPlusClient.getAccountName();
				Image personPhoto = currentPerson.getImage();
				String urlimage = personPhoto.getUrl();

				editor_signin.putString("username", name);
				editor_signin.putString("email", emailid);

				System.out.println(urlimage);

				new DownloadProfilePicTask().execute(urlimage);

				editor_signin.putBoolean("check_signin", true);
				editor_signin.commit();
				
				SignInActivity.this.finish();
				Intent i = new Intent(this, RegisterActivity.class);
				startActivity(i);

			} else {


				System.out.println("Null Error Handled");
				System.out.println("Trying sign out since null error");

				SharedPreferences app_preferences_signin = PreferenceManager
						.getDefaultSharedPreferences(SignInActivity.this);
				SharedPreferences.Editor editor_signin = app_preferences_signin
						.edit();
				editor_signin.putBoolean(
						"check_signin", false);
				editor_signin.putBoolean(
						"check_logout", true);				
				editor_signin.putString(
						"username", "Pakshi");
				editor_signin.putString(
						"email", "Pakshi");
				editor_signin.commit();
				
				this.finish();
				Intent i = new Intent(SignInActivity.this,SignInActivity.class);
				startActivity(i);

			}

		} else {

			// ////// logging out /////////
			if (mPlusClient.isConnected()) {
				mPlusClient.clearDefaultAccount();
				mPlusClient.disconnect();
				mPlusClient.connect();

				editor_signin.putBoolean("check_logout", false);
				editor_signin.commit();
			}
			// ////////////////////////////

		}

	}

	@Override
	public void onDisconnected() {
		mSignInStatus.setText(R.string.loading_status);
		mPlusClient.connect();
		updateButtons(false /* isSignedIn */);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		mConnectionResult = result;
		updateButtons(false /* isSignedIn */);
	}

	private void updateButtons(boolean isSignedIn) {
		if (isSignedIn) {
				
			mSignInButton.setVisibility(View.INVISIBLE);
			//mSignOutButton.setEnabled(true);

		} else {
			if (mConnectionResult == null) {
				// Disable the sign-in button until onConnectionFailed is called
				// with result.
				mSignInButton.setVisibility(View.INVISIBLE);
				mSignInStatus.setText(getString(R.string.loading_status));
			} else {
				// Enable the sign-in button since a connection result is
				// available.
				mSignInButton.setVisibility(View.VISIBLE);
				mSignInStatus.setText(getString(R.string.signed_out_status));
			}

			//mSignOutButton.setEnabled(false);

		}
	}

	// ////////////////// Async Task /////////////////////////

	private class DownloadProfilePicTask extends
			AsyncTask<String, Integer, Void> {

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected Void doInBackground(String... params) {

			// /////////////// Store Profile Picture Android ////////////
			String[] imageurl = params[0].split("\\?");

			System.out.println("MyUrl");
			System.out.println(params[0]);
			System.out.println(imageurl[0]);

			URL url = null;
			try {
				url = new URL(imageurl[0]);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			InputStream input = null;
			try {
				input = url.openStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				
				File direct = new File(Environment.getExternalStorageDirectory()+ "/PakshiFiles");

	    		if(!direct.exists())
	    		{
	    			if(direct.mkdir()); //directory is created;

	    		}

				
				OutputStream output = new FileOutputStream(
						Environment.getExternalStorageDirectory()
								+ "/PakshiFiles/ProfilePic.png");
				try {
					byte[] buffer = new byte[1024];
					int bytesRead = 0;
					while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
						output.write(buffer, 0, bytesRead);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						output.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					input.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// //////////////////////////////////////////////////////////

			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {

		}

		@Override
		protected void onPostExecute(Void result) {

		}
	}





	
	


	}



	

