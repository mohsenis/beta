package com.library.model;

public class Tract {
	private String tractId;
	private String name;
	private String longname;
    private long population;
    private long housing;
    private double latitude;
    private double longitude;
    private long landarea;
    private long waterarea;   
    
    public Tract(){
    }

    public Tract(Tract t) {
		this.tractId = t.tractId;
		this.name = t.name;
		this.longname = t.longname;
		this.population = t.population;
		this.housing = t.housing;
		this.latitude = t.latitude;
		this.longitude = t.longitude;
		this.landarea = t.landarea;
		this.waterarea = t.waterarea;
	}


	public String getTractId() {
		return tractId;
	}


	public void setTractId(String tractId) {
		this.tractId = tractId;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getLongname() {
		return longname;
	}


	public void setLongname(String longname) {
		this.longname = longname;
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
