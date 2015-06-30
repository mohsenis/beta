package com.library.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@XmlRootElement (name = "PnrInCounty")
public class PnrInCounty {
	
	@XmlAttribute
	@JsonSerialize
	public String pnrId;
	
	@XmlAttribute
	@JsonSerialize
	public String lotName;
	
	@XmlAttribute
	@JsonSerialize
	public String city;
	
	@XmlAttribute
	@JsonSerialize
	public String location;
	
	@XmlAttribute
	@JsonSerialize
	public String spaces;
	
	@XmlAttribute
	@JsonSerialize
	public String accessibleSpaces;
	
	@XmlAttribute
	@JsonSerialize
	public String transitServices;
	
	@XmlAttribute
	@JsonSerialize
	public String lat;
	
	@XmlAttribute
	@JsonSerialize
	public String lon;
	
	@XmlAttribute
	@JsonSerialize
	public String metadata;
}
