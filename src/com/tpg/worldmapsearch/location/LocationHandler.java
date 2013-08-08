package com.tpg.worldmapsearch.location;



import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.tpg.worldmapsearch.ui.LocationMapActivity;


public class LocationHandler implements LocationListener,ConnectionCallbacks, OnConnectionFailedListener {
	static String TAG="CustomMaps";
	LocationClient locationClient;		
	LocationRequest location_request;
	final int UPDATE_INTERVAL=10*1000;			//Every 10 seconds
	final int FASTEST_INTERVAL=5*1000;
	LocationMapActivity activity;
	LatLng currentLatLng;
		
	public LocationHandler(LocationMapActivity activity){
		this.activity = activity;
	}
	public void getLocation(){
		locationClient=new LocationClient(activity.getBaseContext(), this, this);
		
		if(locationClient==null){
			Log.d(TAG,"Location Client is null");
			return;
		}
		location_request=LocationRequest.create();
		
		if(location_request==null){
			Log.d(TAG,"Location Request is null");
			return;
		}
			
		Log.d(TAG,"Obtained LocationRequest instance: "+location_request);
		
		location_request.setInterval(UPDATE_INTERVAL);
		location_request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		location_request.setFastestInterval(FASTEST_INTERVAL);
		locationClient.connect();
	}
	/**
	 * The incoming argument is a Location object containing the location's latitude and longitude. 
	 */
	public void onLocationChanged(Location location) {

		String msg ="Updated Location: "+Double.toString(location.getLatitude()) + "," +Double.toString(location.getLongitude());

		Log.d(TAG, msg);
		// Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
		currentLatLng = new LatLng(location.getLatitude(),location.getLongitude());
		locationClient.disconnect();
		location_request=null;
		Log.d(TAG, "Location client is disconnected");	
		activity.updateLocation(currentLatLng);
	}

	public void onProviderDisabled(String provider) {
		Log.d(TAG, "OnProviderDisabled");
	}

	public void onProviderEnabled(String provider) {
		Log.d(TAG, "OnProviderEnabled");
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(TAG,"Status changed");
	}
	/**
	 * Below are the call back methods of ConnectionCallbacks
	 */
	public void onConnected(Bundle bundle) {
		Log.d(TAG,"Connected to Location Services.");
		//Toast.makeText(activity,"connected", Toast.LENGTH_SHORT);
		
		if(locationClient!=null){
			locationClient.requestLocationUpdates(location_request, this);
		}
	}
	/*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
	public void onDisconnected() {
		Log.d(TAG,"Disconnected from Location Services.");
		//Toast.makeText(activity, "onDisconnected", Toast.LENGTH_SHORT);
	}
	 /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     * See the implementation in both the cases
     */
	public void onConnectionFailed(ConnectionResult arg0) {
		Log.d(TAG, "Connection failed");
	}
}
