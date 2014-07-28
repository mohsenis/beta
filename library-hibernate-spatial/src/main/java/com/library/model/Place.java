package com.library.model;


public class Place {
	private String placeId;
	private String name;
    private long population;
    private long housing;
    private double latitude;
    private double longitude;
    private long landarea;
    private long waterarea;
    
    public Place(){    	
    }
    
    public Place(Place p) {
		this.placeId = p.placeId;
		this.name = p.name;
		this.population = p.population;
		this.housing = p.housing;
		this.latitude = p.latitude;
		this.longitude = p.longitude;
		this.landarea = p.landarea;
		this.waterarea = p.waterarea;
	}

	public String getPlaceId() {
		return placeId;
	}

	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getPopulation() {
		return population;
	}

	public void setPopulation(long population) {
		this.population = population;
	}

	public long getHousing() {
		return housing;
	}

	public void setHousing(long housing) {
		this.housing = housing;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public long getLandarea() {
		return landarea;
	}

	public void setLandarea(long landarea) {
		this.landarea = landarea;
	}

	public long getWaterarea() {
		return waterarea;
	}

	public void setWaterarea(long waterarea) {
		this.waterarea = waterarea;
	}    
}
