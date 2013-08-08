package com.tpg.worldmapsearch.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.tpg.worldmapsearch.util.Constants;

public class WorldmapDBHandler {
	private String country;
	private String country_BoundryData;
	public WorldmapDBHandler(){
		
	}
	public WorldmapDBHandler(String country, String country_BoundryData) {
		
		this.country = country;
		this.country_BoundryData = country_BoundryData;
	}
	
	
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getCountry_BoundryData() {
		return country_BoundryData;
	}
	public void setCountry_BoundryData(String country_BoundryData) {
		this.country_BoundryData = country_BoundryData;
	}
	
	/**
	 * @param db
	 * saves the worldmap in db
	 */
	public void save(SQLiteDatabase db) {
		ContentValues cv = new ContentValues();
		
		cv.put(Constants.COUNTRY_NAME, country);
		cv.put(Constants.COUNTRY_BOUNDRY_POINTS, country_BoundryData);
		db.insert(""+Constants.TABLE_BOUNDRY_DATA,Constants.COUNTRY_NAME, cv);
		
	}
	/**
	 * @param db
	 * deletes the worldmap from db.
	 */
	public void delete(SQLiteDatabase db) {
		String args[] = {country};
		ContentValues cv = new ContentValues();
		
		cv.put(Constants.COUNTRY_NAME, country);
		db.delete(Constants.TABLE_BOUNDRY_DATA, Constants.COUNTRY_NAME+"=?", args);
	}

	/**
	 * @param db
	 * @return cursor containg all the worldmap from the db
	 */
	public synchronized Cursor  getAll(SQLiteDatabase db) {
		return db.rawQuery("SELECT "+Constants.COUNTRY_NAME+" FROM "+ Constants.TABLE_BOUNDRY_DATA, null);
	}
	/**
	 * @param db
	 * @return cursor containg all the worldmap from the db
	 */
	public synchronized Cursor  getCountryPoint(SQLiteDatabase db,String countryname) {
		Log.i("Cursor", ""+("SELECT "+Constants.COUNTRY_BOUNDRY_POINTS+" FROM "+ Constants.TABLE_BOUNDRY_DATA+ " WHERE "+Constants.COUNTRY_NAME+" = "+"'"+countryname+"'"));
		
	return db.rawQuery("SELECT "+Constants.COUNTRY_BOUNDRY_POINTS+" FROM "+ Constants.TABLE_BOUNDRY_DATA+ " WHERE "+Constants.COUNTRY_NAME+" = "+"'"+countryname+"'", null);
	
	}
}
