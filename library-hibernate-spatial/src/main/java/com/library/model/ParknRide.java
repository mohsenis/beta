package com.library.model;

import java.util.List;

import com.vividsolutions.jts.geom.Point;

public class ParknRide {

	private int pnrid;
	private double lat;
	private double lon;
	private String location;
	private String lotname;
	private String city;
	private String county;
	private int spaces;
	private int zipcode;
	private int accessiblespaces;
	private String transitservice;
	private String countyid;
	private Point geom;
//	private List<GeoStop> stops;
//	private List<GeoStopRouteMap> routes;
	
	public ParknRide(){
		
	}
	
	public ParknRide(ParknRide p){
		this.pnrid=p.pnrid;
		this.lat=p.lat;
		this.lon=p.lon;
		this.location=p.location;
		this.lotname=p.lotname;
		this.city=p.city;
		this.county=p.county;
		this.zipcode=p.zipcode;
		this.spaces=p.spaces;
		this.accessiblespaces=p.accessiblespaces;
		this.transitservice=p.transitservice;
		this.countyid=p.countyid;
		this.geom=p.geom;
//		this.stops=p.stops;
//		this.routes=p.routes;
	}
	
	public void setPnrid(int i){
		this.pnrid=i;
	}
	
	public void setLat(double i){
		this.lat=i;
	}
	
	public void setLon(double i){
		this.lon=i;
	}
	
	public void setLotname(String s){
		this.lotname=s;
	}
	
	public void setLocation(String s){
		this.location=s;
	}
	
	public void setCity(String s){
		this.city=s;
	}
	
	public void setCounty(String s){
		this.county=s;
	}
	
	public void setSpaces(int i){
		this.spaces=i;
	}
	
	public void setZipcode(int i){
		this.zipcode=i;
	}
	
	public void setAccessiblespaces(int i){
		this.accessiblespaces=i;
	}
	
	public void setTransitservice(String s){
		this.transitservice=s;
	}
	
	public void setCountyid(String s){
		this.countyid=s;
	}
	
	public void setGeom(Point p){
		this.geom=p;
	}
	
/*	public void setStops(List<GeoStop> l){
		this.stops=l;
	}
	
	public void setRoutes(List<GeoStopRouteMap> l){
		this.routes=l;
	}*/
	
	
	
	public int getPnrid(){
		return pnrid;
	}

	public double getLat(){
		return lat;
	}
	
	public double getLon(){
		return lon;
	}
	
	public String getLocation(){
		return location;
	}
	
	public String getLotname(){
		return lotname;
	}
	
	public String getCity(){
		return city;
	}
	
	public String getCounty(){
		return county;
	}

	public int getSpaces(){
		return this.spaces;
	}
	
	public int getZipcode(){
		return zipcode;
	}

	public int getAccessiblespaces(){
		return accessiblespaces;
	}
	
	public String getTransitservice(){
		return transitservice;
	}
	
	public String getCountyid(){
		return countyid;
	}
	
	public Point getGeom(){
		return geom;
	}
	
/*	public List<GeoStop> getStops(){
		return this.stops;
	}
	
	public List<GeoStopRouteMap> getRoutes(){
		return this.routes;
	}*/
}
