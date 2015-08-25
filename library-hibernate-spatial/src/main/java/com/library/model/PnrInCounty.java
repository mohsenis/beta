package com.library.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@XmlRootElement (name = "PnrInCounty")
public class PnrInCounty {
	
	@XmlAttribute
	@JsonSerialize
	public String pnrid;
	
	@XmlAttribute
	@JsonSerialize
	public String county;
	
	@XmlAttribute
	@JsonSerialize
	public String lotname;
	
	@XmlAttribute
	@JsonSerialize
	public String city;
	
	@XmlAttribute
	@JsonSerialize
	public String location;
	
	@XmlAttribute
	@JsonSerialize
	public String zipcode;
	
	@XmlAttribute
	@JsonSerialize
	public String spaces;
	
	@XmlAttribute
	@JsonSerialize
	public String accessiblespaces;
	
	@XmlAttribute
	@JsonSerialize
	public String transitservices;
	
	@XmlAttribute
	@JsonSerialize
	public String lat;
	
	@XmlAttribute
	@JsonSerialize
	public String lon;
	
	@XmlAttribute
	@JsonSerialize
	public String bikerackspaces;
	
	@XmlAttribute
	@JsonSerialize
	public String bikelockerspaces;
	
	@XmlAttribute
	@JsonSerialize
	public String electricvehiclespaces;
	
	@XmlAttribute
	@JsonSerialize
	public String carsharing;
	
	@XmlAttribute
	@JsonSerialize
	public String availability;
	
	@XmlAttribute
	@JsonSerialize
	public String timelimit;
	
	@XmlAttribute
	@JsonSerialize
	public String restroom;
	
	@XmlAttribute
	@JsonSerialize
	public String benches;
	
	@XmlAttribute
	@JsonSerialize
	public String shelter;
	
	@XmlAttribute
	@JsonSerialize
	public String indoorwaitingarea;
	
	@XmlAttribute
	@JsonSerialize
	public String trashcan;
	
	@XmlAttribute
	@JsonSerialize
	public String lighting;
	
	@XmlAttribute
	@JsonSerialize
	public String securitycameras;
	
	@XmlAttribute
	@JsonSerialize
	public String sidewalks;
	
	@XmlAttribute
	@JsonSerialize
	public String pnrsignage;
	
	@XmlAttribute
	@JsonSerialize
	public String lotsurface;
	
	@XmlAttribute
	@JsonSerialize
	public String propertyowner;
	
	@XmlAttribute
	@JsonSerialize
	public String localexpert;
	
	@XmlAttribute
	@JsonSerialize
	public String metadata;
}