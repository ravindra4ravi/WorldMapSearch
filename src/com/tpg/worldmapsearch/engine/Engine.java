package com.tpg.worldmapsearch.engine;

import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;
import com.tpg.worldmapsearch.bean.Place;

public class Engine {
	private static Engine engine;
	private String countryName;
	private LatLng userlatLong;
	private String userFullAddress;
	private ArrayList<Place> list_Places;

	public ArrayList<Place> getList_Places() {
		return list_Places;
	}

	public void setList_Places(ArrayList<Place> list_Places) {
		this.list_Places = list_Places;
	}

	public String getUserFullAddress() {
		return userFullAddress;
	}

	public void setUserFullAddress(String userFullAddress) {
		this.userFullAddress = userFullAddress;
	}

	public LatLng getUserlatLong() {
		return userlatLong;
	}

	public void setUserlatLong(LatLng userlatLong) {
		this.userlatLong = userlatLong;
	}

	public String getCountryName() {
		return countryName;
	}

	private ArrayList<ArrayList<LatLng>> listOfBoundries;

	private Engine() {

	}

	public static Engine getInstance() {
		if (engine == null)
			engine = new Engine();
		return engine;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public ArrayList<ArrayList<LatLng>> getListOfBoundries() {
		return listOfBoundries;
	}

	public void setListOfBoundries(ArrayList<ArrayList<LatLng>> listOfBoundries) {
		this.listOfBoundries = listOfBoundries;
	}
}
