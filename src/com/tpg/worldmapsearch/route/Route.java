package com.tpg.worldmapsearch.route;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Html;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tpg.http.HttpListener;
import com.tpg.http.HttpRequest;
import com.tpg.worldmapsearch.ui.LocationMapActivity;

public class Route implements HttpListener {

	GoogleMap mMap;
	Activity context;
	String lang;

	public static String LANGUAGE_SPANISH = "es";
	public static String LANGUAGE_ENGLISH = "en";
	public static String LANGUAGE_FRENCH = "fr";
	public static String LANGUAGE_GERMAN = "de";
	public static String LANGUAGE_CHINESE_SIMPLIFIED = "zh-CN";
	public static String LANGUAGE_CHINESE_TRADITIONAL = "zh-TW";
	//private ProgressDialog progressDialog;
	Handler handler =new Handler();
	public void  drawRoute(GoogleMap map, Activity c,
			ArrayList<LatLng> points, String language,
			String mode) {
		mMap = map;
		context = c;
		lang = language;
		if (mode == null)
			mode = "driving";
		
			final String url = makeURL(points.get(0).latitude,
					points.get(0).longitude, points.get(1).latitude,
					points.get(1).longitude, mode);
	
		/*	progressDialog = new ProgressDialog(context);
			progressDialog.setMessage("Fetching route, Please wait...");
			progressDialog.setIndeterminate(true);*/
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					HttpRequest request = new HttpRequest();
					request.addHttpListener(Route.this);
					request.get(url);
				}
			}).start();
				
				/*@Override
				public void run() {
					// TODO Auto-generated method stub
					//progressDialog.show();
					
					
					//progressDialog.hide();
				}
			});*/

	}

	

	private String makeURL(double sourcelat, double sourcelog, double destlat,
			double destlog, String mode) {
		StringBuilder urlString = new StringBuilder();

		if (mode == null)
			mode = "driving";

		urlString.append("http://maps.googleapis.com/maps/api/directions/json");
		urlString.append("?origin=");// from
		urlString.append(Double.toString(sourcelat));
		urlString.append(",");
		urlString.append(Double.toString(sourcelog));
		urlString.append("&destination=");// to
		urlString.append(Double.toString(destlat));
		urlString.append(",");
		urlString.append(Double.toString(destlog));
		urlString.append("&sensor=false&mode=" + mode
				+ "&alternatives=true&language=" + lang);
		return urlString.toString();
	}

	private List<LatLng> decodePoly(String encoded) {

		List<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng((((double) lat / 1E5)),
					(((double) lng / 1E5)));
			poly.add(p);
		}

		return poly;
	}


	private void drawPath(String result) {

		try {
			// Tranform the string into a json object
			Log.i("Route", "result == "+result);
			final JSONObject json = new JSONObject(result);
			JSONArray routeArray = json.getJSONArray("routes");
			JSONObject routes = routeArray.getJSONObject(0);
			JSONObject overviewPolylines = routes
					.getJSONObject("overview_polyline");
			String encodedString = overviewPolylines.getString("points");
			List<LatLng> list = decodePoly(encodedString);
           
			for (int z = 0; z < list.size() - 1; z++) {
				LatLng src = list.get(z);
				LatLng dest = list.get(z + 1);
				Polyline line = mMap.addPolyline(new PolylineOptions()
						.add(new LatLng(src.latitude, src.longitude),
								new LatLng(dest.latitude, dest.longitude))
						.width(10).color(Color.GREEN).geodesic(true));
			}
         
           
			JSONArray arrayLegs = routes.getJSONArray("legs");
			JSONObject legs = arrayLegs.getJSONObject(0);
			JSONObject objdistance = legs.getJSONObject("distance");
			JSONObject objtarvelTime = legs.getJSONObject("duration");
		    String distance = objdistance.getString("text");
		    String travelTime= objtarvelTime.getString("text");
		    ((LocationMapActivity)context).updateHeaderInfo(distance,travelTime);
				
			

		} catch (JSONException e) {
            Log.i("ROUTE", "exception == "+e.getMessage());
		}
	}

	/**
	 * Class that represent every step of the directions. It store distance,
	 * location and instructions
	 */
	private class Step {
		public String distance;
		public LatLng location;
		public String instructions;

		Step(JSONObject stepJSON) {
			JSONObject startLocation;
			try {

				distance = stepJSON.getJSONObject("distance").getString("text");
				startLocation = stepJSON.getJSONObject("start_location");
				location = new LatLng(startLocation.getDouble("lat"),
						startLocation.getDouble("lng"));
				try {
					instructions = URLDecoder.decode(
							Html.fromHtml(
									stepJSON.getString("html_instructions"))
									.toString(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				;

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void notifyResponse(final HttpRequest obj) {
		// TODO Auto-generated method stub
		//progressDialog.hide();
		((LocationMapActivity)context).cancelProgressDialog();
		if (obj.getResult() != null) {
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					drawPath(obj.getResult());
				}
			});
					
		
			
		}
	}

}
