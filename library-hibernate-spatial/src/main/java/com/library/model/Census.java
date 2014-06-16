package com.library.model;

import com.vividsolutions.jts.geom.Point;

public class Census {
	private String blockId;
	private String placeId;
	private String districtId;
	private String regionId;
	private String urbanId;	
    private int population;
    private double latitude;
    private double longitude;
    private int landarea;
    private int waterarea;
    private String poptype;
    private Point location;
    
    public Census() {
    }
    
    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }
    
    public String getBlockId() {
        return blockId;
    }  
    
    public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}
    
    public String getPlaceId() {
		return placeId;
	}

	public String getDistrictId() {
		return districtId;
	}

	public void setDistrictId(String districtId) {
		this.districtId = districtId;
	}

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public String getUrbanId() {
		return urbanId;
	}

	public void setUrbanId(String urbanId) {
		this.urbanId = urbanId;
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
