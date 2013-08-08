package com.tpg.worldmapsearch.xml;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

import com.tpg.worldmapsearch.bean.Place;

public class GooglePlaceXMLParser {
	String TAG = "GooglePlaceXMLParser";
	private String xmlTobeParse;
	private ArrayList<Place> list_Places;

	public GooglePlaceXMLParser(String xmlTobeParse) {
		this.xmlTobeParse = xmlTobeParse;
	}

	public ArrayList<Place> getPlacesList() {
		list_Places = new ArrayList<Place>();
		XmlPullParser xpp = null;
		XmlPullParserFactory factory = null;
		try {
			factory = XmlPullParserFactory.newInstance();

			factory.setNamespaceAware(true);

			xpp = factory.newPullParser();

			InputStream in = new ByteArrayInputStream(xmlTobeParse.getBytes());

			// read it with BufferedReader
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			xpp.setInput(in, null);
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {

				if (eventType == XmlPullParser.START_TAG
						&& xpp.getName().equals("result")) {
					eventType = xpp.next();
					String id = "";
					String name = "";
					String vicinity = "";
					String types = "";
					String locationLat = "";
					String locationLng = "";
					String viewport = "";
					String icon = "";
					String reference = "";
					while (!(eventType == XmlPullParser.END_TAG && xpp
							.getName().equals("result"))) {
						if (eventType == XmlPullParser.START_TAG
								&& xpp.getName().equals("name"))
							name = xpp.nextText();
						else if (eventType == XmlPullParser.START_TAG
								&& xpp.getName().equals("vicinity"))
							vicinity = xpp.nextText();
						else if (eventType == XmlPullParser.START_TAG
								&& xpp.getName().equals("type"))
							types = types == "" ? xpp.nextText() : types + " "
									+ xpp.nextText();
						else if (eventType == XmlPullParser.START_TAG
								&& xpp.getName().equals("lat"))
							locationLat = xpp.nextText();
						else if (eventType == XmlPullParser.START_TAG
								&& xpp.getName().equals("lng"))
							locationLng = xpp.nextText();
						else if (eventType == XmlPullParser.START_TAG
								&& xpp.getName().equals("icon"))
							icon = xpp.nextText();
						else if (eventType == XmlPullParser.START_TAG
								&& xpp.getName().equals("reference"))
							reference = xpp.nextText();
						else if (eventType == XmlPullParser.START_TAG
								&& xpp.getName().equals("id"))
							id = xpp.nextText();
						eventType = xpp.next();
					}

					// Add each new place to the Places Content Provider
					Place place = new Place();
					place.id = id;
					place.name = name;
					place.vicinity = vicinity;
					place.types = types;
					place.locationLat = locationLat;
					place.locationLng = locationLng;
					place.viewport = viewport;
					place.icon = icon;
					place.reference = reference;
					list_Places.add(place);
				}
				eventType = xpp.next();
			}

		} catch (MalformedURLException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		} catch (XmlPullParserException e) {
			Log.e(TAG, e.getMessage());
		} finally {
		}
		return list_Places;
	}
}
