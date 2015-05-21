package com.library.model;

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
	public long UrbanPopulation;
	
	@XmlAttribute
	@JsonSerialize
	public long RuralPopulation;
	
	@XmlAttribute
	@JsonSerialize
	public int TotalBlocks;
	
	@XmlAttribute
	@JsonSerialize
	public int TotalTracts;
	
	@XmlAttribute
	@JsonSerialize
	public long TotalLandArea;
	
	@XmlElement(name = "MapCL")
    public Collection<MapCounty> MapCounties = new ArrayList<MapCounty>();
}
