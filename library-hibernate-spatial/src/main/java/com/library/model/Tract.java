package com.library.model;

public class Tract {
	private String tractId;
    private long population;
    private long housing;
    private double latitude;
    private double longitude;
    private long landarea;
    private long waterarea;   

    public Tract() {
    }


	public String getTractId() {
		return tractId;
	}


	public void setTractId(String tractId) {
		this.tractId = tractId;
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
