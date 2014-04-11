package com.suny.ocr.network;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.suny.ocr.R;


public class AndroidRESTClientActivity extends Activity {
	private static final String SERVICE_URL = "http://192.168.0.2:8080/whereami-server/webapi/location";
	//private static final String SERVICE_URL = "http://10.12.14.238:8080/whereami-server/webapi/location";
	//private static final String SERVICE_URL = "http://223.194.199.18:80/whereami-server/webapi/location";
	//private static final String SERVICE_URL = "http://10.12.26.58:8080/whereami-server/webapi/location";
	 
    private static final String TAG = "AndroidRESTClientActivity";
    
     
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ocr_result);
        
        Intent intent = getIntent();
        String lat = intent.getStringExtra("LAT");
        String lon = intent.getStringExtra("LON");
        String query = intent.getStringExtra("QUERY");
        
        
        query="SUNY Korea";
        
       /* lat = "37.592075";
		lon = "126.683438";
		query = "신토오리" + "&" + "커피베이";*/
		
		getLocation(lat, lon, query);
    }
    
    public void doOCR(View view) {
    	
    }

    public void getLocation(String lat, String lon, String query) {
    	query = query.replace(" ", "%20");
    	String url = SERVICE_URL + "/" + lat + "/" + lon + "/" + query;    	   	
    	String rawResult = "";
        try {
            HttpParams httpParameters = new BasicHttpParams();
            int timeoutConnection = 5000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 5000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
            HttpGet getRequest = new HttpGet(url);
            getRequest.addHeader("accept", "text/plain");
            
            HttpResponse response = httpClient.execute(getRequest);
            
            rawResult = getResult(response).toString();
            httpClient.getConnectionManager().shutdown();
            
            String[] result = rawResult.split(",");
            int flag = (int) Integer.parseInt(result[0]);
            if (flag == 1) {
            	Intent returnIntent = new Intent();        	
        		returnIntent.putExtra("LAT", result[1]);
        		returnIntent.putExtra("LON", result[2]);
        		setResult(RESULT_OK, returnIntent);
            } else {
            	Intent returnIntent = new Intent();
        		setResult(RESULT_CANCELED, returnIntent);
            }
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } 
        
        finish();         
	}
    
    
    @SuppressWarnings("deprecation")
	private void uploadFile(String filePath, String fileName) {
    	 
    	  InputStream inputStream;
    	  try {
    	    inputStream = new FileInputStream(new File(filePath));
    	    byte[] data;
    	    try {
    	      data = IOUtils.toByteArray(inputStream);
    	 
    	      String url = SERVICE_URL + "/upload";
    	      HttpClient httpClient = new DefaultHttpClient();
    	      HttpPost httpPost = new HttpPost(url);
    	 
    	      InputStreamBody inputStreamBody = new InputStreamBody(new ByteArrayInputStream(data), fileName);
    	      MultipartEntity multipartEntity = new MultipartEntity();
    	      multipartEntity.addPart("file", inputStreamBody);
    	      httpPost.setEntity(multipartEntity);
    	 
    	      HttpResponse httpResponse = httpClient.execute(httpPost);
    	 
    	      // Handle response back from script.
    	      if(httpResponse != null) {
    	 
    	      } else { // Error, no response.
    	 
    	      }
    	    } catch (IOException e) {
    	      e.printStackTrace();
    	    }
    	  } catch (FileNotFoundException e1) {
    	    e1.printStackTrace();
    	  }
    	}
    
    private StringBuilder getResult(HttpResponse response) throws IllegalStateException, IOException {
        StringBuilder result = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())), 1024);
        String output;
        while ((output = br.readLine()) != null) 
            result.append(output);

        return result;      
  }    
    
    public void post(String url, List<NameValuePair> nameValuePairs) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
        HttpPost httpPost = new HttpPost(url);

        try {
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            for(int index=0; index < nameValuePairs.size(); index++) {
                if(nameValuePairs.get(index).getName().equalsIgnoreCase("image")) {
                    // If the key equals to "image", we use FileBody to transfer the data
                    entity.addPart(nameValuePairs.get(index).getName(), new FileBody(new File (nameValuePairs.get(index).getValue())));
                } else {
                    // Normal string data
                    entity.addPart(nameValuePairs.get(index).getName(), new StringBody(nameValuePairs.get(index).getValue()));
                }
            }

            httpPost.setEntity(entity);

            HttpResponse response = httpClient.execute(httpPost, localContext);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
