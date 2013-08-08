package com.tpg.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class HttpRequest {
	HttpListener listener;
	String result;
	public int REQUEST_ID = 0;
	public int REQ_ID = 0;
    public int RESPONSE_CODE = -1;

    public HttpRequest() {
		// TODO Auto-generated constructor stub
	}
	public void addHttpListener(HttpListener listener){
    	this.listener = listener;
    }
	public void get(String url) {

        // Making HTTP request
		InputStream is = null;
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            RESPONSE_CODE = httpResponse.getStatusLine().getStatusCode();
            is = httpEntity.getContent();           

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            result = sb.toString();
            
            is.close();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }finally{
        	
        }
       // Log.d("JSON_RUTA", json);
        listener.notifyResponse(this);

    }

	public void post(String url,final List<NameValuePair> list, final Hashtable<String, String> header) {
		try {
			// https ssl code for accepting all certificates
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			
			schemeRegistry.register(new Scheme("https",
					new EasySSLSocketFactory(), 443));

			HttpParams params = new BasicHttpParams();

			params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
			params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
			params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE,
					new ConnPerRouteBean(30));
			params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			ClientConnectionManager cm = new SingleClientConnManager(params,
					schemeRegistry);

			HttpClient httpclient = new DefaultHttpClient(cm, params);
			HttpPost request = new HttpPost(url.trim());
			if(header!=null){
				Enumeration enums=header.keys();
				while (enums.hasMoreElements()) {
					String key = (String) enums.nextElement();
					
					String value=header.get(key);
					request.addHeader(key, value);
				}
			}
			 
			JSONObject requestJSON = new JSONObject();
			requestJSON.put("email", "dev@ticketpro.com");
			requestJSON.put("password", "password");
			
			StringEntity requestEntity = new StringEntity(requestJSON.toString(), "UTF-8");  
			request.setEntity(requestEntity);
	         
	         try {
	        	 if(list!=null)
	        		 request.setEntity(new UrlEncodedFormEntity(list));
			} catch (Exception e) {
				
				e.printStackTrace();
			} 

			HttpResponse response = httpclient.execute(request);
			Log.i("URL ", "" + url.trim());
			HttpEntity resEntityGet = response.getEntity();
			if (resEntityGet != null) {
				result = EntityUtils.toString(resEntityGet);
			}

		} catch (ClientProtocolException e) {
			result = "error Client protocal " + e.getMessage();
			Log.i("Error message : ", "" + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			result = "error IO exception " + e.getMessage();
			Log.i("Error message : ", "" + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("Error message : ", "" + e.getMessage());
			result = "error " + e.getMessage();
		} finally {
			listener.notifyResponse(this);
		}

	}

	public String getResult() {
		return result;
	}

	void getFile(String url,String path,String filename){
		try {
			URL ur = new URL(url);
			HttpURLConnection urlConnection = (HttpURLConnection) ur.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			urlConnection.connect();
			File file;
			FileOutputStream fos;
			if(path!=null){
				 file = new File(path,filename);
				 fos = new FileOutputStream(file);
			}else{
				fos = ((Context)listener).openFileOutput(filename, Context.MODE_PRIVATE);
			}
			InputStream is = urlConnection.getInputStream();
			int totalSize = urlConnection.getContentLength();
			int downloadedSize = 0;
			byte[] buffer = new byte[1024];
			int bufferLength = 0;
		
			while ( (bufferLength = is.read(buffer)) > 0 ) 

			{

			//add the data in the buffer to the file in the file output stream (the file on the sd card

			fos.write(buffer, 0, bufferLength);

			//add up the size so we know how much is downloaded

			downloadedSize += bufferLength;

			int progress=(int)(downloadedSize*100/totalSize);

			//this is where you would do something to report the prgress, like this maybe

			//updateProgress(downloadedSize, totalSize);

			}

			//close the output stream when done

			fos.close();
			 
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			
		}
	}
}
