package com.pakshi.pakshiapp;

import java.io.InputStream;
import java.net.URL;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.view.View;

public class NotificationView extends Activity {

	ImageView image;
	Bitmap bitmap;
	ProgressDialog pDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notification);
	}

	@Override
	protected void onStart() {
		super.onStart(); // Always call the superclass method first
		setContentView(R.layout.notification);

		// Uri url = getIntent().getData();

		Bundle extras = getIntent().getExtras();

		// Extract data using passed keys
		String value = extras.getString("message");
		String arrayString[] = value.split("\\s+");

		String url = arrayString[0];
        String bird_info = "";
		int size = arrayString.length;

		for (int i = 1; i < size; i++) {
			bird_info = bird_info.concat(" " + arrayString[i]);
		}

		image = (ImageView) findViewById(R.id.birdview);

		GetXMLTask task = new GetXMLTask();
		// Execute the task
		task.execute(new String[] { url });

		// WebView myWebView = (WebView) findViewById(R.id.birdview);
		// myWebView.loadUrl(value);

		TextView label = (TextView) findViewById(R.id.show_data);
		label.setText(bird_info);

	}

	@Override
	public void onStop() {
		super.onStop();
	}

	private class GetXMLTask extends AsyncTask<String, Void, Bitmap> {

		// Sets the Bitmap returned by doInBackground
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(NotificationView.this);
			pDialog.setMessage("Retrieving the image ....");
			pDialog.show();
		}

		@Override
		protected Bitmap doInBackground(String... urls) {
			Bitmap map = null;
			for (String url : urls) {
				map = downloadImage(url);
			}
			return map;
		}

		protected void onPostExecute(Bitmap result) {
			if (result != null) {
				pDialog.dismiss();
				image.setImageBitmap(result);
			} else {
				pDialog.dismiss();
				Toast.makeText(
						NotificationView.this,
						"Failed to retrieve the contents. Check again for internet connection",
						Toast.LENGTH_SHORT).show();
			}
		}

		// Creates Bitmap from InputStream and returns it
		private Bitmap downloadImage(String url) {
			Bitmap bitmap = null;
			InputStream stream = null;
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inSampleSize = 1;

			try {
				stream = getHttpConnection(url);
				bitmap = BitmapFactory.decodeStream(stream, null, bmOptions);
				stream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return bitmap;
		}

		// Makes HttpURLConnection and returns InputStream
		private InputStream getHttpConnection(String urlString)
				throws IOException {
			InputStream stream = null;
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();

			try {
				HttpURLConnection httpConnection = (HttpURLConnection) connection;
				httpConnection.setRequestMethod("GET");
				httpConnection.connect();

				if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					stream = httpConnection.getInputStream();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return stream;
		}
	}
}
