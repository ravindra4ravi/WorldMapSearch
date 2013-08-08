package com.tpg.worldmapsearch.bean;

import java.io.Serializable;

public class Place implements Serializable {
	public  String id ;
	public  String name ;
	public  String vicinity;
	public  String types;
	public   String locationLat ;
	public  String locationLng ;
	public   String viewport ;
	public   String icon ;
	public  String reference ;
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "id == "+id+" name == "+name+" vicinity == "+vicinity+"  types == "+types+"  locationlat == "+locationLat+"  locationlong == "+locationLng+"  viewport == "+viewport+"  icon ==  "+icon+"  refrence == "+reference;
	}
}
