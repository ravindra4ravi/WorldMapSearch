package com.tpg.worldmapsearch.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;
import com.tpg.worldmapsearch.engine.Engine;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

public class WorldmapSearchUtil {
	final static String TAG = "WorldmapSearchUtil";
	final  static String MULTI_POLYGON = "MULTIPOLYGON";
	final static String POLYGON = "POLYGON";
	
	public static  void copyDataBase(Context context  ,String dbPath, String dbName) throws IOException{
		//File file = new File(dbPath, dbName);
		Log.i(TAG, "dbPath  == "+dbPath);
		//String dbPath   = "/data/data/"+ context.getApplicationContext().getPackageName()+ "/databases/";
    	InputStream myInput = context.getAssets().open(dbName);
    	String outFileName = dbPath ;
    	OutputStream myOutput = new FileOutputStream(outFileName);
    	//new FileOut
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer, 0, length);
    	}
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
    	
 
    }
	/**
	 * function to check if database is already exist
	 * @return true if database is already created otherwise false
	 */
	/*public static  boolean checkDataBase(Context appContext){
		SQLiteDatabase checkDB = null;
		
		String DB_PATH = "/data/data/"+ appContext.getApplicationContext().getPackageName()+ "/databases/";
		try {
			String pathToDb = DB_PATH + Constants.DATABASE_NAME;
			
			if(!new File(pathToDb).exists()){
				return false;
			}
			
			checkDB = SQLiteDatabase.openDatabase(pathToDb, null,
					SQLiteDatabase.OPEN_READWRITE|SQLiteDatabase.NO_LOCALIZED_COLLATORS);

		}catch (SQLiteException e) {
			//Utility.printLog(getClass().getName(),"checkDataBase() : checking if the database already exist"+e);
			checkDB = null;
			return false;
		}finally{
			if(db!=null){
				if(db.isOpen()){
					
					db.close();
					db = null;
				}
			}
		}
		if (checkDB != null) {
			db = checkDB;
		}
		
		return checkDB != null ? true : false;
	} */
	
	public static boolean isConnectingToInternet(Context _context){
        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
          if (connectivity != null) 
          {
              NetworkInfo[] info = connectivity.getAllNetworkInfo();
              if (info != null) 
                  for (int i = 0; i < info.length; i++) 
                      if (info[i].getState() == NetworkInfo.State.CONNECTED)
                      {
                          return true;
                      }
 
          }
          return false;
    }
	public static  void parseLatLong(String cordinates) {
		ArrayList<ArrayList<LatLng>> listOfBoundries = new ArrayList<ArrayList<LatLng>>();
		ArrayList<LatLng> listCountryBoundryLatLong = new ArrayList<LatLng>();
		StringBuilder builder = new StringBuilder(cordinates);
		String points[] = null;
		if (cordinates.startsWith(POLYGON)) {
			builder.delete(0, POLYGON.length() + 2);
			builder.delete(builder.length() - 2, builder.length());
			points = new String[1];
			points[0] = builder.toString();

		} else if (cordinates.startsWith(MULTI_POLYGON)) {
			builder.delete(0, MULTI_POLYGON.length() + 3);
			builder.delete(builder.length() - 3, builder.length());
			cordinates = new String(builder);

			points = cordinates.split(",\\(\\(");
			int index = 0;
			for (String string : points)
				points[index++] = string.replace("))", "");

		}

		for (String polyLines : points) {
			String latLongpoints[] = polyLines.split(",");
			listCountryBoundryLatLong = new ArrayList<LatLng>();
			for (String latlong : latLongpoints) {
				String latlongPoints[] = latlong.split(" ");
				LatLng latilongi = new LatLng(new Double(
						latlongPoints[1].trim()), new Double(
						latlongPoints[0].trim()));

				listCountryBoundryLatLong.add(latilongi);
			}
			listOfBoundries.add(listCountryBoundryLatLong);
		}
		Engine.getInstance().setListOfBoundries(listOfBoundries);
	}
}

