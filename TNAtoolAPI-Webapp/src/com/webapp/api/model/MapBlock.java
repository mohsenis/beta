package com.webapp.api.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@XmlRootElement(name = "MapB")
public class MapBlock {
	@XmlAttribute
	@JsonSerialize
	public String ID;
	
	@XmlAttribute
	@JsonSerialize
	public String Population;
	
	@XmlAttribute
	@JsonSerialize
	public String LandArea;
	
	@XmlAttribute
	@JsonSerialize
	public String Lat;
	
	@XmlAttribute
	@JsonSerialize
	public String Lng;
	
	@XmlAttribute
	@JsonSerialize
	public String County;
	
}
