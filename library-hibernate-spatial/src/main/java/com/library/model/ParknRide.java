package com.library.model;

import java.util.List;

import com.vividsolutions.jts.geom.Point;

public class ParknRide {

	private int pnrid;
	private double lat;
	private double lon;
	private String lotname;
	private String location;
	private String city;
	private int zipcode;
	private String countyid;
	private String county;
	private int spaces;
	private int accessiblespaces;
	private int bikerackspaces;
	private int bikelockerspaces;
	private int electricvehiclespaces;
	private String carsharing;
	private String transitservice;
	private String availability;
	private String timelimit;
	private String restroom;
	private String benches;
	private String shelter;
	private String indoorwaitingarea;
	private String trashcan;
	private String lighting;
	private String securitycameras;
	private String sidewalks;
	private String pnrsignage;
	private String lotsurface; 
	private String propertyowner; 
	private String localexpert;
	private Point geom;
//	private List<GeoStop> stops;
//	private List<GeoStopRouteMap> routes;
	
	public ParknRide(){
		
	}
	
	public ParknRide(ParknRide p){
		this.pnrid=p.pnrid;
		this.lat=p.lat;
		this.lon=p.lon;
		this.lotname=p.lotname;
		this.location=p.location;
		this.city=p.city;
		this.zipcode=p.zipcode;
		this.countyid=p.countyid;
		this.county=p.county;
		this.spaces=p.spaces;
		this.accessiblespaces=p.accessiblespaces;
		this.bikelockerspaces=p.bikelockerspaces;
		this.bikerackspaces=p.bikerackspaces;
		this.electricvehiclespaces=p.electricvehiclespaces;
		this.carsharing=p.carsharing;
		this.transitservice=p.transitservice;
		this.availability=p.availability;
		this.timelimit=p.timelimit;
		this.restroom=p.restroom;
		this.benches=p.benches;
		this.shelter=p.shelter;
		this.indoorwaitingarea=p.indoorwaitingarea;
		this.trashcan=p.trashcan;
		this.lighting=p.lighting;
		this.securitycameras=p.securitycameras;
		this.sidewalks=p.sidewalks;
		this.pnrsignage=p.pnrsignage;
		this.lotsurface=p.lotsurface;
		this.propertyowner=p.propertyowner;
		this.localexpert=p.localexpert;
		this.geom=p.geom;
	}
	
	/*
	 * Beginning of setter.
	 */
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
	
	public void setZipcode(int i){
		this.zipcode=i;
	}
	
	public void setCounty(String s){
		this.county=s;
	}
	
	public void setCountyid(String s){
		this.countyid=s;
	}
	
	public void setSpaces(int i){
		this.spaces=i;
	}
	
	public void setAccessiblespaces(int i){
		this.accessiblespaces=i;
	}
	
	public void setBikerackspaces(int i){
		this.bikerackspaces=i;
	}
	
	public void setBikelockerspaces(int i){
		this.bikelockerspaces=i;
	}
	
	public void setElectricvehiclespaces(int i){
		this.electricvehiclespaces=i;
	}
	
	public void setCarsharing(String s){
		this.carsharing=s;
	}
	
	public void setTransitservice(String s){
		this.transitservice=s;
	}
	
	public void setAvailability(String s){
		this.availability=s;
	}
	
	
	public void setTimelimit(String s){
		this.timelimit=s;
	}
	
	
	public void setRestroom(String s){
		this.restroom=s;
	}
	
	
	public void setBenches(String s){
		this.benches=s;
	}
	
	
	public void setShelter(String s){
		this.shelter=s;
	}
	
	
	public void setIndoorwaitingarea(String s){
		this.indoorwaitingarea=s;
	}
	
	public void setTrashcan(String s){
		this.trashcan=s;
	}
	
	public void setLighting(String s){
		this.lighting=s;
	}
	
	public void setSecuritycameras(String s){
		this.securitycameras=s;
	}
	
	
	public void setSidewalks(String s){
		this.sidewalks=s;
	}
	
	public void setPnrsignage(String s){
		this.pnrsignage=s;
	}
	
	public void setLotsurface(String s){
		this.lotsurface=s;
	}
	
	public void setPropertyowner(String s){
		this.propertyowner=s;
	}
	
	public void setLocalexpert(String s){
		this.localexpert=s;
	}
	
	public void setGeom(Point p){
		this.geom=p;
	}

	
	
	/*
	 * Beginning of getters.
	 */
	public int getPnrid(){
		return pnrid;
	}

	public double getLat(){
		return lat;
	}
	
	public double getLon(){
		return lon;
	}
	
	public String getLotname(){
		return lotname;
	}
	
	public String getLocation(){
		return location;
	}
	
	public String getCity(){
		return city;
	}
	
	public int getZipcode(){
		return zipcode;
	}

	public String getCounty(){
		return county;
	}

	public String getCountyid(){
		return countyid;
	}
	
	public int getSpaces(){
		return this.spaces;
	}
	
	public int getAccessiblespaces(){
		return accessiblespaces;
	}
	
	public int getBikerackspaces(){
		return bikerackspaces;
	}
	
	public int getBikelockerspaces(){
		return bikelockerspaces;
	}
	
	public int getElectricvehiclespaces(){
		 return electricvehiclespaces;
	}
	
	public String getCarsharing(){
		return carsharing;
	}
	
	public String getTransitservice(){
		return transitservice;
	}
	
	public String getAvailability(){
		return availability;
	}
	
	public String getTimelimit(){
		return timelimit;
	}
	
	public String getRestroom(){
		return restroom;
	}
	
	public String getBenches(){
		return benches;
	}
	
	public String getShelter(){
		return shelter;
	}
	
	public String getIndoorwaitingarea(){
		return indoorwaitingarea;
	}
	
	public String getTrashcan(){
		return trashcan;
	}
	
	public String getLighting(){
		return lighting;
	}
	
	public String getSecuritycameras(){
		return securitycameras;
	}
	
	public String getSidewalks(){
		return sidewalks;
	}
	
	public String getPnrsignage(){
		return pnrsignage;
	}
	
	public String getLotsurface(){
		return lotsurface;
	}
	
	public String getPropertyowner(){
		return propertyowner;
	}
	
	public String getLocalexpert(){
		return localexpert;
	}
	
	public Point getGeom(){
		return geom;
	}

}
