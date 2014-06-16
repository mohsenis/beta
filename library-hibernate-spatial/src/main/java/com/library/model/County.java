package com.library.model;


public class County {
	private String countyId;
	private String name;
	private long population;
    private long housing;
    private double latitude;
    private double longitude;
    private long landarea;
    private long waterarea;
    private String regionId;
    private String regionName;
    
	public County() {		
	}

	public String getCountyId() {
		return countyId;
	}

	public void setCountyId(String countyId) {
		this.countyId = countyId;
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

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	
	  
}
