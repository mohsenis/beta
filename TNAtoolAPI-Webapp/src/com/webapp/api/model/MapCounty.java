package com.webapp.api.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@XmlRootElement(name = "MapC")
public class MapCounty {
	@XmlAttribute
	@JsonSerialize
	public String Id;
	
	@XmlAttribute
	@JsonSerialize
	public String Name;
	
	@XmlAttribute
	@JsonSerialize
	public double Poopulation;
	
	@XmlElement(name = "MapTL")
    public Collection<MapTract> MapTracts = new ArrayList<MapTract>();

	@XmlElement(name = "MapBL")
    public Collection<MapBlock> MapBlocks = new ArrayList<MapBlock>();
}
