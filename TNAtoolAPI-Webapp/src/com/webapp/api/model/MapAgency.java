package com.webapp.api.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@XmlRootElement(name = "MapA")
public class MapAgency {
	@XmlAttribute
	@JsonSerialize
	public String Id;
	
	@XmlAttribute
	@JsonSerialize
	public String Name;
	
	@XmlAttribute
	@JsonSerialize
	public int ServiceStop=0;
	
	@XmlElement(name = "MapRL")
    public Collection<MapRoute> MapRoutes = new ArrayList<MapRoute>();

	@XmlElement(name = "MapSL")
    public Collection<MapStop> MapStops = new ArrayList<MapStop>();
}
