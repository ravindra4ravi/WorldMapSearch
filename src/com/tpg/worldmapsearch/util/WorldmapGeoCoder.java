package com.tpg.worldmapsearch.util;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

public class WorldmapGeoCoder {
	public static Address getAddressFromGeoCoder(Context context,
			String strAddress) {

		Geocoder coder = new Geocoder(context.getApplicationContext(),
				Locale.getDefault());
		List<Address> address = null;
		try {
			address = coder.getFromLocationName(strAddress, 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
         
		return ((address != null  && address.size()> 0) ? address.get(0) : null);
	}

	public static Address getAddressFromGeoCoder(Context context, LatLng latlong) {

		Geocoder coder = new Geocoder(context.getApplicationContext(),
				Locale.getDefault());
		List<Address> address = null;
		try {
			address = coder.getFromLocation(latlong.latitude,
					latlong.longitude, 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}

		return ((address != null && address.size()> 0) ? address.get(0) : null);
	}

}
