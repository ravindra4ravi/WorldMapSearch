package com.tpg.worldmapsearch.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.tpg.http.HttpListener;
import com.tpg.http.HttpRequest;
import com.tpg.worldmapsearch.bean.Item;
import com.tpg.worldmapsearch.bean.Place;
import com.tpg.worldmapsearch.db.SQLiteHelper;
import com.tpg.worldmapsearch.db.WorldmapDBHandler;
import com.tpg.worldmapsearch.engine.Engine;
import com.tpg.worldmapsearch.location.LocationHandler;
import com.tpg.worldmapsearch.location.LocationUpdate;
import com.tpg.worldmapsearch.route.Route;
import com.tpg.worldmapsearch.ui.componenet.PlaceListAdapter;
import com.tpg.worldmapsearch.util.Constants;
import com.tpg.worldmapsearch.util.WorldmapGeoCoder;
import com.tpg.worldmapsearch.util.WorldmapSearchUtil;
import com.tpg.worldmapsearch.xml.GooglePlaceXMLParser;

/**
 * @author ravindraprajapati
 *
 */
public class LocationMapActivity extends SherlockFragmentActivity implements
		OnClickListener, HttpListener, LocationUpdate {

	private GoogleMap mMap;
	private Button btnRouteLocater, btnPlaceLocater, btnSearchPlace;
	private Button btnSubmit, btnCancel, btnSearch;
	private String TAG = "LocationMapActivity";
	private Handler handler = null;
	private ProgressDialog pd = null;
	private Dialog dialog;
	private EditText edtFrom;
	private EditText edtTo;
	private EditText edtCustomSearch;
	private CheckBox checkUserLocation;
	private Spinner spinnerTravelMode;
	private TextView txtDistance, txtDuration;
	private LinearLayout lnrHeaderInfo;
	private ArrayList<Place> list_Places = null;
	private SQLiteDatabase db;
	private int placeCategoryIcon[] = { R.drawable.airport, R.drawable.atm,
			R.drawable.bank, R.drawable.bar, R.drawable.school,
			R.drawable.police };
private View view ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_locationmap);
	//	view = findViewById(R.layout.activity_locationmap);
		// / header info
		Log.i(TAG, "oncreate");
		getSherlock().getActionBar().setBackgroundDrawable(
				new ColorDrawable(Color.parseColor("#0377d3")));
		lnrHeaderInfo = (LinearLayout) findViewById(R.id.lnrheader);
		
		txtDistance = (TextView) findViewById(R.id.txtDistance);
		txtDuration = (TextView) findViewById(R.id.txttravelTime);

		btnRouteLocater = (Button) findViewById(R.id.btnRouteLocator);
		btnPlaceLocater = (Button) findViewById(R.id.btnPlaceLocator);
		btnSearchPlace = (Button) findViewById(R.id.btnSearchPlace);

		btnPlaceLocater.setOnClickListener(this);
		btnRouteLocater.setOnClickListener(this);
		btnSearchPlace.setOnClickListener(this);
		handler = new Handler();
		pd = new ProgressDialog(this);
		pd.setMessage("Retriving information...");
		if (db != null) {
			if (db.isOpen())
				db.close();
			db = null;
		}
		db = (new SQLiteHelper(this).getWritableDatabase());
		setUpMapIfNeeded();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.world_map_search, menu);
		final MenuItem item = menu.findItem(R.id.menu_share);
		// Log.i(TAG,
		// "item == " + item + " action view == " + item.getActionView());
		item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// TODO Auto-generated method stub
				// showActionBarDialog();
				locationSearchDialog();
				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * @Override protected void onResume() { super.onResume();
	 * setUpMapIfNeeded(); }
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultcode,
			Intent intent) {
		// TODO Auto-generated method stub
		
		if (list_Places == null) {
			list_Places = Engine.getInstance().getList_Places();
		}
		if ( resultcode == RESULT_OK && requestCode == Constants.PLACE_DETAILS_ACTIVITY_CODE ) {
			if (intent.getSerializableExtra(Constants.PLACE_DETAILS) != null) {

				Place place = (Place) intent
						.getSerializableExtra(Constants.PLACE_DETAILS);
				
				showPlaces();
				Route route = new Route();
				ArrayList<LatLng> points = new ArrayList<LatLng>();
				points.add(new LatLng(Double.parseDouble(place.locationLat),
						Double.parseDouble(place.locationLng)));
				points.add(Engine.getInstance().getUserlatLong());
				if(intent.getExtras().getBoolean(Constants.CLICK_ON_DIRECTION_BUTTON)){
				pd.show();
				route.drawRoute(mMap, this, points, Route.LANGUAGE_ENGLISH,
						null);
				}
			}
		}else {
			showPlaces();
		}
		super.onActivityResult(requestCode, resultcode, intent);
	}

	private void setUpMapIfNeeded() {

		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				setUpMap();
			}
		}
	}

	/**
	 * This is where we can add markers or lines, add listeners or move the
	 * camera. In this case, we just add a marker near Africa.
	 * <p>
	 * This should only be called once and when we are sure that {@link #mMap}
	 * is not null.
	 */
	private void setUpMap() {
		mMap.clear();
		if (Engine.getInstance().getUserlatLong() != null) {
			MarkerOptions marker = new MarkerOptions();
			LatLng latlong = new LatLng(
					Engine.getInstance().getUserlatLong().latitude, Engine
							.getInstance().getUserlatLong().longitude);
			marker.position(latlong);
			marker.title((Engine.getInstance().getUserFullAddress() != null) ? Engine
					.getInstance().getUserFullAddress() : Engine.getInstance()
					.getCountryName());
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlong, 3));
			mMap.addMarker(marker);
		}
		if(Engine.getInstance().getList_Places() != null){
			list_Places = Engine.getInstance().getList_Places();
			showPlaces();
		}
		drawContryBoundry(Engine.getInstance().getListOfBoundries());

	}

	private void drawContryBoundry(ArrayList<ArrayList<LatLng>> listOfBoundries) {

		for (ArrayList<LatLng> latLng : listOfBoundries) {

			mMap.addPolygon(new PolygonOptions().addAll(latLng)
					.strokeColor(Color.RED).strokeWidth(2));
		}

	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		lnrHeaderInfo.setVisibility(View.GONE);
		if (view == btnPlaceLocater) {
			showPlaceTypeListDialog();
		} else if (view == btnRouteLocater) {
			routeLocatorDialog();
		} else if (view == btnCancel) {
			cancelAlertDialog();
		} else if (view == btnSearchPlace) {
			locationSearchDialog();
		} else if (view == btnSubmit) {
			pd.show();
			handler.post(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					requestDirection(edtFrom.getText().toString(), edtTo
							.getText().toString(), (String) spinnerTravelMode
							.getSelectedItem());
					// pd.cancel();
				}
			});
		} else if (view == btnSearch) {

			if (checkUserLocation.isChecked()) {
				cancelAlertDialog();
				pd.show();
				LocationHandler locationhandler = new LocationHandler(this);
				locationhandler.getLocation();
			} else {
				handler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (checkUserLocation.isChecked()) {

						} else {
							if (edtCustomSearch.getText().toString().length() == 0) {
								Toast.makeText(LocationMapActivity.this,
										"Please enter location.",
										Toast.LENGTH_SHORT).show();
							} else {
								cancelAlertDialog();
								pd.show();
								requestForSearchLocation(edtCustomSearch
										.getText().toString());
							}
						}
						pd.cancel();
					}
				});
			}
		}

	}

	private void showActionBarDialog() {

		final AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
		final AlertDialog alertActionDialog = builderSingle.create();
		alertActionDialog.setIcon(R.drawable.ic_launcher);

		alertActionDialog.setTitle("Search");
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				LocationMapActivity.this, android.R.layout.simple_list_item_1);
		arrayAdapter.add("Route Locater");
		arrayAdapter.add("Place Locater");
		arrayAdapter.add("Search Location");

		final ListView listView = new ListView(getApplicationContext());
		listView.setBackgroundColor(Color.WHITE);
		listView.setScrollingCacheEnabled(false);
		listView.setAdapter(arrayAdapter);

		alertActionDialog.setView(listView);
		alertActionDialog.show();
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					final int pos, long arg3) {
				// TODO Auto-generated method stub

				alertActionDialog.cancel();

				// TODO Auto-generated method stub
				if (pos == 0) {
					routeLocatorDialog();
				} else if (pos == 1) {
					showPlaceTypeListDialog();
				} else {
					locationSearchDialog();
				}

			}

		});

		// TODO Auto-generated method stub

	}

	private void locationSearchDialog() {
		dialog = new Dialog(this);
		View view = getLayoutInflater().inflate(R.layout.custom_search, null);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(view);

		edtCustomSearch = (EditText) view.findViewById(R.id.edtSearch);

		btnSearch = (Button) view.findViewById(R.id.btnSearch);
		checkUserLocation = (CheckBox) view.findViewById(R.id.chekesearch);
		final InputMethodManager imm = (InputMethodManager) this
				.getSystemService(Service.INPUT_METHOD_SERVICE);
		checkUserLocation
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub

						if (isChecked) {
							edtCustomSearch.setInputType(0);
							edtCustomSearch.setFocusable(false);
							edtCustomSearch.setFocusableInTouchMode(false);
							edtCustomSearch.setClickable(false);
							imm.hideSoftInputFromWindow(
									edtCustomSearch.getWindowToken(), 0);
						} else {
							edtCustomSearch.setInputType(1);
							edtCustomSearch.setFocusable(true);
							edtCustomSearch.setFocusableInTouchMode(true);
							edtCustomSearch.setClickable(true);
						}
					}
				});
		dialog.show();
		btnSearch.setOnClickListener(this);

		// TODO Auto-generated method stub

	}

	private void routeLocatorDialog() {

		dialog = new Dialog(this);
		View view = getLayoutInflater().inflate(R.layout.route_locator, null);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(view);
		((TextView) view.findViewById(R.id.dialogname))
				.setText("Get Direction");
		edtFrom = (EditText) view.findViewById(R.id.edtFrom);
		edtTo = (EditText) view.findViewById(R.id.edtTo);
		spinnerTravelMode = (Spinner) view.findViewById(R.id.spinnerMode);
		btnSubmit = (Button) view.findViewById(R.id.btnGetDirection);
		btnCancel = (Button) view.findViewById(R.id.btnCancel);
		edtFrom.setText((Engine.getInstance().getUserFullAddress() != null) ? Engine
				.getInstance().getUserFullAddress() : Engine.getInstance()
				.getCountryName());
		dialog.show();
		btnSubmit.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		// TODO Auto-generated method stub

	}

	private void showPlaceTypeListDialog() {

		dialog = new Dialog(this);
		View view = getLayoutInflater().inflate(R.layout.place_category_dialog,
				null);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(view);
		((TextView) view.findViewById(R.id.dialogname))
				.setText("Place Category");
		String place_List[] = getResources().getStringArray(R.array.place_List);
		List<Object> list_Items = new ArrayList<Object>();
		int index = 0;
		for (String placeType : place_List) {
			Item item = new Item(placeType, placeCategoryIcon[index++]);
			list_Items.add(item);
		}
		final PlaceListAdapter placeListAdapter = new PlaceListAdapter(this,
				list_Items);
		final ListView listView = (ListView) view
				.findViewById(R.id.listCategory);
		listView.setBackgroundColor(Color.WHITE);
		listView.setScrollingCacheEnabled(false);
		listView.setAdapter(placeListAdapter);

		dialog.show();
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				// TODO Auto-generated method stub

				cancelAlertDialog();
				final String type = ((Item) listView.getAdapter().getItem(pos)).mTitle;
				pd.show();
				new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						requestForPlaces(type);
						pd.cancel();
					}
				}).start();

			}

			private void requestForPlaces(String type) {

				String locationStr = Engine.getInstance().getUserlatLong().latitude
						+ "," + Engine.getInstance().getUserlatLong().longitude;
				String baseURI = "https://maps.googleapis.com/maps/api/place/search/xml?sensor=true";

				String placesFeed = (type.equals("")) ? baseURI + "&location="
						+ locationStr + "&radius=" + "5000" + "&key="
						+ getResources().getString(R.string.googleapikey)
						: baseURI
								+ "&location="
								+ locationStr
								+ "&rankby=distance"
								+ "&types="
								+ type
								+ "&key="
								+ getResources().getString(
										R.string.googleapikey);
				HttpRequest httpReq = new HttpRequest();
				httpReq.addHttpListener(LocationMapActivity.this);
				httpReq.get(placesFeed);
			}
		});

		// TODO Auto-generated method stub

	}

	public void updateHeaderInfo(String strDistance, String strDuration) {
       
		txtDistance.setText(((strDistance!=null && strDistance.length() >0)?strDistance:"N/A" ));
		txtDuration.setText( ((strDuration!=null && strDuration.length() >0)?strDuration:"N/A" ));
		lnrHeaderInfo.setVisibility(View.VISIBLE);

	}

	public void cancelProgressDialog() {
		pd.cancel();
	}

	private void cancelAlertDialog() {
		dialog.cancel();
		dialog.dismiss();
	}

	private void requestForSearchLocation(String location) {

		LatLng currentLatLong = null;
		String strCountryName = "";
		String fullAddress = "";

		Address address = WorldmapGeoCoder.getAddressFromGeoCoder(
				getApplicationContext(), location);
		if(address == null){
			Toast.makeText(this, "Search address not found",
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (address != null) {
			address =WorldmapGeoCoder.getAddressFromGeoCoder(this,new LatLng(address.getLatitude(), address.getLongitude()));
			if(address == null){
				Toast.makeText(this, "Search address not found",
						Toast.LENGTH_SHORT).show();
				return;
			}
			strCountryName = address.getCountryName();
			currentLatLong = new LatLng(address.getLatitude(),
					address.getLongitude());
			fullAddress = "";
			for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {

				if (address.getAddressLine(i) != null) {
					fullAddress += address.getAddressLine(i);
					if (i < address.getMaxAddressLineIndex() - 1)
						fullAddress += ",";
				}
			}
			Engine.getInstance().setUserlatLong(currentLatLong);
			Engine.getInstance().setCountryName(strCountryName);
			Engine.getInstance().setUserFullAddress(fullAddress);
			WorldmapDBHandler worldmapDBHandler = new WorldmapDBHandler();
			Cursor cur = worldmapDBHandler.getCountryPoint(db, strCountryName);
			cur.moveToFirst();

			WorldmapSearchUtil.parseLatLong(cur.getString(0));
			cur.close();
			// db.close();
		}

		if (currentLatLong == null) {
			Toast.makeText(this, "Source address is not found",
					Toast.LENGTH_SHORT).show();
		} else {
			showUserLocationOnMap();

		}

	}

	private void showUserLocationOnMap() {
		mMap.clear();
		MarkerOptions marker = new MarkerOptions();
		LatLng latlong = new LatLng(
				Engine.getInstance().getUserlatLong().latitude, Engine
						.getInstance().getUserlatLong().longitude);
		marker.position(latlong);
		marker.title((Engine.getInstance().getUserFullAddress() != null) ? Engine
				.getInstance().getUserFullAddress() : Engine.getInstance()
				.getCountryName());
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlong, 3));
		mMap.addMarker(marker);
		drawContryBoundry(Engine.getInstance().getListOfBoundries());
	}

	@SuppressWarnings("unused")
	private void requestDirection(String from, String to, String type) {
		if (from.length() == 0) {
			cancelProgressDialog();
			  Toast.makeText(this, "Please enter source location.",
					Toast.LENGTH_SHORT).show();
			  return;
		} if (to.length() == 0) {
			cancelProgressDialog();
			Toast.makeText(this, "Please enter destination location.",
					Toast.LENGTH_SHORT).show();
			 return;
		} 
			cancelAlertDialog();
			LatLng latlongFrom = null;
			String strCountryName = "";
			String fullAddressFrom = "";
			LatLng latlongTo = null;
			String fullAddressTo = "";
			String strCountryNameTo = "";
			if (from.equalsIgnoreCase(Engine.getInstance().getUserFullAddress())) {
				latlongFrom = new LatLng(
						Engine.getInstance().getUserlatLong().latitude, Engine
								.getInstance().getUserlatLong().longitude);
				strCountryName = Engine.getInstance().getCountryName();
				fullAddressFrom = Engine.getInstance().getUserFullAddress();
			}
			if (latlongFrom == null) {
				Address addressFrom = WorldmapGeoCoder.getAddressFromGeoCoder(
						getApplicationContext(), from);
				if (addressFrom != null) {
					strCountryName = addressFrom.getCountryName();
					latlongFrom = new LatLng(addressFrom.getLatitude(),
							addressFrom.getLongitude());
					fullAddressFrom = "";
					for (int i = 0; i < addressFrom.getMaxAddressLineIndex(); i++) {
						fullAddressFrom += addressFrom.getAddressLine(i);
					}
				}
			}
			Address addressTo = WorldmapGeoCoder.getAddressFromGeoCoder(
					getApplicationContext(), to);
			if (addressTo != null) {
				strCountryNameTo = addressTo.getCountryName();
				latlongTo = new LatLng(addressTo.getLatitude(),
						addressTo.getLongitude());

				for (int i = 0; i < addressTo.getMaxAddressLineIndex(); i++) {
					fullAddressTo += addressTo.getAddressLine(i);

				}
			}
			if (latlongFrom == null) {
				cancelProgressDialog();
				Toast.makeText(this, "Source address is not found",
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (latlongTo == null) {
				cancelProgressDialog();
				Toast.makeText(this, "Destination address is not found",
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (latlongFrom != null && latlongTo != null) {
				mMap.clear();
				MarkerOptions markersource = new MarkerOptions();
				markersource.position(latlongFrom);
				markersource
						.title("" + fullAddressFrom + ", " + strCountryName);
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlongFrom,
						8));
				mMap.addMarker(markersource);

				MarkerOptions markerdest = new MarkerOptions();
				markerdest.position(latlongTo);
				markerdest.title("" + fullAddressTo + ", " + strCountryNameTo);
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlongTo, 8));
				mMap.addMarker(markerdest);
				drawContryBoundry(Engine.getInstance().getListOfBoundries());
				Route route = new Route();
				ArrayList<LatLng> points = new ArrayList<LatLng>();
				points.add(latlongFrom);
				points.add(latlongTo);
				route.drawRoute(mMap, this, points, Route.LANGUAGE_ENGLISH,
						(String) spinnerTravelMode.getSelectedItem());
			}

		
	}

	@Override
	public void notifyResponse(final HttpRequest obj) {
		// TODO Auto-generated method stub
		Log.i(TAG, "markerTitle  == " + obj.getResult());
		handler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (obj.RESPONSE_CODE == 200) {
					GooglePlaceXMLParser parser = new GooglePlaceXMLParser(obj
							.getResult());
					list_Places = parser.getPlacesList();
					Engine.getInstance().setList_Places(list_Places);
					showPlaces();
				} else {

					Toast.makeText(
							LocationMapActivity.this,
							"Either you check your internet connection or try after some time.",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

	}

	private Place getPlace(LatLng currentLatlong) {
		Log.i("marker", "marker size == " + list_Places.size());
		for (Place place : list_Places) {

			LatLng latlon = new LatLng(Double.parseDouble(place.locationLat
					.trim()), Double.parseDouble(place.locationLng.trim()));
			currentLatlong = new LatLng(currentLatlong.latitude,
					currentLatlong.longitude);
			Log.i("marker", "marker == " + latlon + "  currentLatlong == "
					+ currentLatlong);
			if (currentLatlong.latitude == latlon.latitude
					&& currentLatlong.longitude == latlon.longitude) {

				return place;
			}

		}
		return null;
	}

	private String getSpilitLatLong(String str) {
		String strarr[] = str.split("\\.");
		String tempStr = strarr[1];
		if (strarr[1].length() > 5)
			tempStr = strarr[1].substring(0, 5);
		return strarr[0] + "." + tempStr;
	}

	@Override
	public void updateLocation(LatLng latlng) {

		// TODO Auto-generated method stub
		// cancelAlertDialog();
		pd.cancel();

		if (latlng != null) {
			try {

				Address address = WorldmapGeoCoder.getAddressFromGeoCoder(this,
						latlng);
				String strCountryName = "";
				LatLng currentLatLong;
				if (address != null) {
					strCountryName = address.getCountryName();
					currentLatLong = new LatLng(address.getLatitude(),
							address.getLongitude());
					String fullAddress = "";
					for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
						if (address.getAddressLine(i) != null) {
							fullAddress += address.getAddressLine(i);
							if (i < address.getMaxAddressLineIndex() - 1)
								fullAddress += ",";
						}
					}
					Engine.getInstance().setUserlatLong(currentLatLong);
					Engine.getInstance().setCountryName(strCountryName);
					Engine.getInstance().setUserFullAddress(fullAddress);
					WorldmapDBHandler worldmapDBHandler = new WorldmapDBHandler();
					Cursor cur = worldmapDBHandler.getCountryPoint(db,
							strCountryName.trim());
					cur.moveToFirst();

					WorldmapSearchUtil.parseLatLong(cur.getString(0));
					cur.close();
					// db.close();
					showUserLocationOnMap();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Toast.makeText(this, "Unable to get your current location.",
					Toast.LENGTH_SHORT).show();
		}

	}

	private void showPlaces() {

		mMap.clear();
		Log.i(TAG, "size == " + list_Places);
		if (list_Places.size() > 0) {

			for (Place place : list_Places) {
				LatLng latlon = new LatLng(Double.parseDouble(place.locationLat
						.trim()), Double.parseDouble(place.locationLng.trim()));
				String markerTitle = place.name + "\n" + place.vicinity;
				MarkerOptions markerOption = new MarkerOptions();
				markerOption.position(latlon);

				markerOption.title(markerTitle);
				Marker marker = mMap.addMarker(markerOption);
				place.locationLat = "" + marker.getPosition().latitude;
				place.locationLng = "" + marker.getPosition().longitude;

			}

		}
		MarkerOptions marker = new MarkerOptions();
		marker.position(Engine.getInstance().getUserlatLong());
		marker.title((Engine.getInstance().getUserFullAddress() != null) ? Engine
				.getInstance().getUserFullAddress() : Engine.getInstance()
				.getCountryName());
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Engine.getInstance()
				.getUserlatLong(), 10));
		marker.snippet("Me");
		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				// TODO Auto-generated method stub
				Log.i(TAG, "snipest == " + marker.getSnippet());
				if (marker.getSnippet() == null) {
					
					Log.i(TAG, "Place == "
							+ getPlace(marker.getPosition()).toString());
					Intent intent = new Intent(LocationMapActivity.this,
							PlaceDetailActivity.class);
					intent.putExtra(Constants.PLACE_DETAILS,
							getPlace(marker.getPosition()));
					startActivityForResult(intent,
							Constants.PLACE_DETAILS_ACTIVITY_CODE);
				} else {
					Toast.makeText(LocationMapActivity.this,
							"That is your location.", Toast.LENGTH_SHORT)
							.show();
				}
			}
		});
		mMap.addMarker(marker);
		drawContryBoundry(Engine.getInstance().getListOfBoundries());
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unbindDrawables();
		super.onDestroy();
		
	}
	private  void unbindDrawables() {
		
		/* getSupportFragmentManager().beginTransaction()
         .remove(getSupportFragmentManager())
         .commit();*/
	}
}
