package com.tpg.worldmapsearch.db;

import com.tpg.worldmapsearch.util.Constants;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author ravindra
 * 
 */
public class SQLiteHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = Constants.DATABASE_NAME;
	private static final int SCHEMA_VERSION = 1;

	/**
	 * @param context
	 *            constructor
	 */
	public SQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, SCHEMA_VERSION);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
	 * .SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE if not exists " + Constants.TABLE_BOUNDRY_DATA
				+ " ("+"_id INTEGER PRIMARY KEY AUTOINCREMENT,"+ Constants.COUNTRY_NAME + " TEXT,"
				+ Constants.COUNTRY_BOUNDRY_POINTS + " TEXT);");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
	 * .SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		android.util.Log.w("BookLoc",
				"Upgrading database, which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_BOUNDRY_DATA);
		onCreate(db);

		/*
		 * todo: make temp table, copy current data to it, fix real table's
		 * schema and then copy the data back
		 */
	}
}
