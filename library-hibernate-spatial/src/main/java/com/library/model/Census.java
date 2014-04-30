package com.library.model;

import com.vividsolutions.jts.geom.Point;

public class Census {
	private String id;
    private int population;
    private double latitude;
    private double longitude;
    private int landarea;
    private int waterarea;
    private String poptype;
    private Point location;

    public Census() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public int getLandarea() {
        return landarea;
    }
    
    public void setLandarea(int landarea) {
        this.landarea = landarea;
    }

    public void setWaterarea(int waterarea) {
        this.waterarea = waterarea;
    }
    
    public int getWaterarea() {
        return waterarea;
    }

    public void setPoptype(String poptype) {
        this.poptype = poptype;
    }
    
    public String getPoptype() {
        return poptype;
    }

    public Point getLocation() {
        return this.location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }
}
