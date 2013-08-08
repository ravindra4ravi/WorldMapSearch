package com.tpg.worldmapsearch.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.tpg.worldmapsearch.db.SQLiteHelper;
import com.tpg.worldmapsearch.db.WorldmapDBHandler;
import com.tpg.worldmapsearch.engine.Engine;
import com.tpg.worldmapsearch.util.Constants;
import com.tpg.worldmapsearch.util.WorldmapGeoCoder;
import com.tpg.worldmapsearch.util.WorldmapSearchUtil;

public class WorldMapSearchActivity extends Activity {

	final String TAG = "WorldMapSearchActivity";
	final String MULTI_POLYGON = "MULTIPOLYGON";
	final String POLYGON = "POLYGON";
	final String DEFAULT_SELECTED_COUNTRY = "United States";
	private ArrayList<String> table_CountryList;
	private ArrayAdapter<String> adapeterSpinner;
	private Spinner spinnerSelectCountry = null;
	private Button btnSearchOnMap = null;
	private String strCountryName = DEFAULT_SELECTED_COUNTRY;
	private LatLng currentLatLong;
	private SQLiteDatabase db;
	private ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_world_map_search);
		/*if(android.os.Build.VERSION.SDK_INT >= 11){
			getActionBar().setBackgroundDrawable(
					new ColorDrawable(Color.parseColor("#0377d3")));
		}*/
		
		btnSearchOnMap = (Button) findViewById(R.id.btnSearchOnMap);
		spinnerSelectCountry = (Spinner) findViewById(R.id.spinnerContry);
		pd = new ProgressDialog(this);
		pd.setMessage("Fetching location...");
		
		if (db != null) {
			if(db.isOpen())
			   db.close();
			db = null;
			
		}
			db = (new SQLiteHelper(this).getWritableDatabase());
		
		
		try {
			if (!PreferenceManager.getDefaultSharedPreferences(this)
					.getBoolean(Constants.COPY_DATABASE, false)){
				WorldmapSearchUtil.copyDataBase(this, db.getPath(),
						Constants.DATABASE_NAME);
			PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(Constants.COPY_DATABASE, true).commit();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.i("DBError", "database error ==  " + e.getMessage());
		}
		WorldmapDBHandler worldmapDBHandler = new WorldmapDBHandler();
		Cursor cursor = worldmapDBHandler.getAll(db);
		String list[] = new String[cursor.getCount()];
		table_CountryList = new ArrayList<String>();
		int index = 0;
		if (cursor != null && cursor.getCount() > 0) {
			if (!cursor.isFirst())
				cursor.moveToFirst();

			do {
				//Log.i(TAG, "cursor.getString(0) == " + cursor.getString(0));
				list[index] = cursor.getString(0);
				index++;
				table_CountryList.add(cursor.getString(0));
				cursor.moveToNext();
			} while (!cursor.isAfterLast());
			cursor.close();
			cursor = null;
		}
		//Log.i(TAG, "cursor.getString(0) == " + strCountryName);

		

		adapeterSpinner = new ArrayAdapter<String>(WorldMapSearchActivity.this,
				android.R.layout.simple_list_item_1, list);
		adapeterSpinner
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerSelectCountry.setAdapter(adapeterSpinner);

		adapeterSpinner.notifyDataSetChanged();

		spinnerSelectCountry
				.setSelection(getSpinnerCountryIndex(DEFAULT_SELECTED_COUNTRY));

		btnSearchOnMap.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				pd.show();
				if (WorldmapSearchUtil
						.isConnectingToInternet(WorldMapSearchActivity.this)) {
					Log.i(TAG, "if    "+WorldmapSearchUtil
							.isConnectingToInternet(WorldMapSearchActivity.this));
					if (findCurrentUserLocation((String) spinnerSelectCountry
							.getSelectedItem())) {
						Log.i(TAG, "if");
						
						WorldmapDBHandler worldmapDBHandler = new WorldmapDBHandler();
						Cursor cur = worldmapDBHandler.getCountryPoint(db, strCountryName);
						cur.moveToFirst();

						WorldmapSearchUtil.parseLatLong(cur.getString(0));
						cur.close();
						//db.close();
						pd.cancel();
						Intent intent = new Intent(getApplicationContext(),
								LocationMapActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
						startActivity(intent);
					}
				} else {
					Log.i(TAG, "else    ");
					pd.cancel();
					Toast.makeText(WorldMapSearchActivity.this,
							"Please check your internet connection.",
							Toast.LENGTH_SHORT).show();
				}
				
			}
		});
	}

	
	private boolean findCurrentUserLocation(String selectedCountry) {
		Log.i(TAG, "address == " + selectedCountry);
		boolean isAddressFound = false;
		Geocoder coder = new Geocoder(getApplicationContext(),
				Locale.getDefault());
		Address address = null;
		try {

			address = WorldmapGeoCoder.getAddressFromGeoCoder(this,
					selectedCountry);
			
			if (address != null) {
				currentLatLong = new LatLng(address.getLatitude(),
						address.getLongitude());
				address =WorldmapGeoCoder.getAddressFromGeoCoder(getApplicationContext(), currentLatLong);
				strCountryName = address.getCountryName();
				
				String fullAddress = "";
				for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
					Log.i(TAG, "address.getAddressLine(i)  == "+address.getAddressLine(i));
					if(address.getAddressLine(i) != null){
						 fullAddress += address.getAddressLine(i);
						 if(i<address.getMaxAddressLineIndex()-1)
							 fullAddress+=",";
					}
				}
				Log.i(TAG, "address == " + address);
				Engine.getInstance().setUserlatLong(currentLatLong);
				Engine.getInstance().setCountryName(strCountryName);
				Engine.getInstance().setUserFullAddress(fullAddress);
				isAddressFound = true;
			}else{
				Toast.makeText(this, "Service not available at this time Please try after some or your internet is not working.", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	//	Log.i(TAG, "address == " + address);
		return isAddressFound;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onPause");
		pd.dismiss();
		super.onPause();
		
		// db.close();
	}
@Override
protected void onDestroy() {
	// TODO Auto-generated method stub
	
	if(db != null){
		if(db.isOpen())
		db.close();
		db = null;
	}
	super.onDestroy();
}
	private int getSpinnerCountryIndex(String countryName) {

		int index = 0;
		index = table_CountryList.indexOf(countryName);
		return index;
	}
	
}
