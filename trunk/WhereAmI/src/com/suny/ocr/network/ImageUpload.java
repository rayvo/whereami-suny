package com.suny.ocr.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class ImageUpload extends Activity {
	private static final int PICK_IMAGE = 1;
	private ImageView imgView;
	private Button upload;
	private EditText caption;
	private Bitmap bitmap;
	private ProgressDialog dialog;
	
	private String[] filePaths;
	private List<String> fileNames;
	String lat, lon;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
         lat = intent.getStringExtra("LAT");
         lon = intent.getStringExtra("LON");
        String names = intent.getStringExtra("FILENAMES");
        filePaths = names.split("&");
        fileNames = new ArrayList<String>();
        String folder = "/sdcard/";
        for (int i = 0; i< filePaths.length; i++) {
        	String path = filePaths[i];
        	fileNames.add(path.substring(path.lastIndexOf("/")));
        }
		new ImageUploadTask().execute();
		
		
	}

	

	
	class ImageUploadTask extends AsyncTask<Void, Void, String> {
		
		@Override
		protected String doInBackground(Void... unsued) {
			try {
				CommClient client = new CommClient();
				client.procUploadFile(lat, lon, fileNames, filePaths, 0);

				return "SUCCESS";
			} catch (ClientProtocolException e) {
				return e.toString();
			} catch (IOException e) {
				return e.toString();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return e.toString();
			}
			
			
		}

		@Override
		protected void onProgressUpdate(Void... unsued) {

		}

		@Override
		protected void onPostExecute(String sResponse) {
			
		}
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			// HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
			// THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
	}

	public void decodeFile(String filePath) {
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, o);

		// The new size we want to scale to
		final int REQUIRED_SIZE = 1024;

		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
				break;
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		bitmap = BitmapFactory.decodeFile(filePath, o2);

		imgView.setImageBitmap(bitmap);

	}
}