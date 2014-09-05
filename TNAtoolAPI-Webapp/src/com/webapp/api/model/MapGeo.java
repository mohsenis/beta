package com.webapp.api.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@XmlRootElement(name = "MapG")
public class MapGeo {
	@XmlAttribute
	@JsonSerialize
	public String TotalPopulation;
	
	@XmlAttribute
	@JsonSerialize
	public String TotalBlocks;
	
	@XmlAttribute
	@JsonSerialize
	public String TotalTracts;
	
	@XmlAttribute
	@JsonSerialize
	public String TotalLandArea;
	
	@XmlElement(name = "MapCL")
    public Collection<MapCounty> MapCounties = new ArrayList<MapCounty>();
}
