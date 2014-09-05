package com.webapp.api.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@XmlRootElement(name = "MapR")
public class MapRoute {
	@XmlAttribute
	@JsonSerialize
	public String Id;
	
	@XmlAttribute
	@JsonSerialize
	public String Name;
	
	@XmlAttribute
	@JsonSerialize
	public boolean hasDirection;
	
	@XmlAttribute
	@JsonSerialize
	public String Shape="";
	
	@XmlAttribute
	@JsonSerialize
	public String Shape0="";
	
	@XmlAttribute
	@JsonSerialize
	public String Shape1="";
	
	@XmlAttribute
	@JsonSerialize
	public String Length;
	
	@XmlAttribute
	@JsonSerialize
	public String AgencyId;
	
	@XmlAttribute
	@JsonSerialize
	public String Fare;
	
	@XmlAttribute
	@JsonSerialize
	public int Frequency;
}
