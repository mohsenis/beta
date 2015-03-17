package com.library.model;


public class CongDist {
	private String congdistId;
	private String name;
    private long population;
    private long housing;
    private double latitude;
    private double longitude;
    private long landarea;
    private long waterarea;
    
    public CongDist() {
    }
    
    public CongDist(CongDist c) {		
		this.congdistId = c.congdistId;
		this.name = c.name;
		this.population = c.population;
		this.housing = c.housing;
		this.latitude = c.latitude;
		this.longitude = c.longitude;
		this.landarea = c.landarea;
		this.waterarea = c.waterarea;
	}

	public String getCongdistId() {
		return congdistId;
	}

	public void setCongdistId(String congdistId) {
		this.congdistId = congdistId;
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
