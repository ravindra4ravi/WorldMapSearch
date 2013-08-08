package com.tpg.worldmapsearch.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.tpg.worldmapsearch.bean.Placemark;

import android.util.Log;

@SuppressWarnings("unused")
public class NavigationSaxHandler extends DefaultHandler {
	// private static final String TAG =
	// NavigationSaxHandler.class.getSimpleName();

	// ===========================================================
	// Fields
	// ===========================================================
	private String TAG = "NavigationSaxHandler";
	private boolean in_kmltag = false;
	private boolean in_placemarktag = false;
	private boolean in_nametag = false;
	private boolean in_descriptiontag = false;
	private boolean in_geometrycollectiontag = false;
	private boolean in_linestringtag = false;
	private boolean in_pointtag = false;
//	private boolean in_coordinatestag = false;
	private boolean in_datatag = false;
	private boolean in_values = false;
	private String attributeName = "";
	//private StringBuffer buffer;

	private NavigationDataSet navigationDataSet = new NavigationDataSet();

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public NavigationDataSet getParsedData() {
		navigationDataSet.setPlacemarks(navigationDataSet.getPlacemarks());
		return this.navigationDataSet;
	}

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
		this.navigationDataSet = new NavigationDataSet();
	}

	@Override
	public void endDocument() throws SAXException {
		// Nothing to do
	}

	/**
	 * Gets be called on opening tags like: <tag> Can provide attribute(s), when
	 * xml was like: <tag attribute="attributeValue">
	 */
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		attributeName = "";
		if (localName.equals("kml")) {
			this.in_kmltag = true;
		} else if (localName.equals("Placemark")) {
			this.in_placemarktag = true;
			// Log.d(TAG, "startElement localName[" + localName + "] qName[" +
			// qName + "]");
			navigationDataSet.setCurrentPlacemark(new Placemark());
		} else if (localName.equals("name")) {
			this.in_nametag = true;
		} else if (localName.equals("description")) {
			this.in_descriptiontag = true;
		} else if (localName.equals("GeometryCollection")) {
			this.in_geometrycollectiontag = true;
		} else if (localName.equals("LineString")) {
			this.in_linestringtag = true;
		} else if (localName.equals("point")) {
			this.in_pointtag = true;
		} else if (localName.equals("Data")) {
			
			if (atts.getValue("", atts.getLocalName(0)).equals("wkt_4326")) {
				attributeName = atts.getValue("", atts.getLocalName(0));
				this.in_datatag = true;
			}
		} else if (localName.equals("value") && in_datatag) {
			in_values = true;
		}

	}

	/**
	 * Gets be called on closing tags like: </tag>
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		if (localName.equals("kml")) {
			this.in_kmltag = false;
		} else if (localName.equals("Placemark")) {
			this.in_placemarktag = false;

			if ("Route".equals(navigationDataSet.getCurrentPlacemark()
					.getTitle()))
				navigationDataSet.setRoutePlacemark(navigationDataSet
						.getCurrentPlacemark());
			else {
				navigationDataSet.addCurrentPlacemark();
			}

		} else if (localName.equals("name")) {
			this.in_nametag = false;
		} else if (localName.equals("description")) {
			this.in_descriptiontag = false;
		} else if (localName.equals("GeometryCollection")) {
			this.in_geometrycollectiontag = false;
		} else if (localName.equals("LineString")) {
			this.in_linestringtag = false;
		} else if (localName.equals("point")) {
			this.in_pointtag = false;
		} else if (localName.equals("Data")) {
			this.in_datatag = false;

		} else if (localName.equals("value")) {
			this.in_values = false;

		}
	}

	/**
	 * Gets be called on the following structure: <tag>characters</tag>
	 */
	@Override
	public void characters(char ch[], int start, int length) {
		if (this.in_nametag) {
			if (navigationDataSet.getCurrentPlacemark() == null)
				navigationDataSet.setCurrentPlacemark(new Placemark());
			navigationDataSet.getCurrentPlacemark().setTitle(
					new String(ch, start, length));
		} else if (this.in_descriptiontag) {
			if (navigationDataSet.getCurrentPlacemark() == null)
				navigationDataSet.setCurrentPlacemark(new Placemark());
			navigationDataSet.getCurrentPlacemark().setDescription(
					new String(ch, start, length));
		} else if (this.in_values) {
			
			String previousData = "";
			if(navigationDataSet.getCurrentPlacemark() != null)
				previousData = navigationDataSet.getCurrentPlacemark().getCoordinates();
			//Log.d(TAG, "Country == "+navigationDataSet.getCurrentPlacemark().getTitle()+"  previousData  == "+previousData);
			StringBuilder sb = new StringBuilder();
			sb.append(ch, start, length);
			StringBuilder builder  = new StringBuilder();
			
			if(previousData!=null  && previousData.length()>0){
				builder.append(previousData)	;
			}
			builder.append(sb.toString());
			navigationDataSet.getCurrentPlacemark().setCoordinates(
					new String(builder.toString()));
		//	Log.d(TAG, "coordinate  == "+navigationDataSet.getCurrentPlacemark().getCoordinates());
         //  }

		}
	}
}