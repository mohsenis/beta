package com.library.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@XmlRootElement( name = "HubCluster")
public class HubCluster {
	@XmlAttribute
	@JsonSerialize
	public String lat;
	
	@XmlAttribute
	@JsonSerialize
	public String lon;
	
	@XmlAttribute
	@JsonSerialize
	public String stopscount;
	
	@XmlAttribute
	@JsonSerialize
	public String pop;

	@XmlAttribute
	@JsonSerialize
	public String agenciescount;
	
	@XmlAttribute
	@JsonSerialize
	public String countiescount;
	
	@XmlAttribute
	@JsonSerialize
	public String routescount;
	
	@XmlAttribute
	@JsonSerialize
	public String visits;
	
	@XmlAttribute
	@JsonSerialize
	public Integer pnrcount;
	
	@XmlAttribute
	@JsonSerialize
	public Integer placescount;
	
	@XmlAttribute
	@JsonSerialize
	public String urbanareaspop;
	
	@XmlElement(name = "countiesNames")
	public List<String> countiesNames;
	
	@XmlElement(name = "agenciesNames")
	public List<String> agenciesNames;
	
	@XmlElement(name = "urbanNames")
	public List<String> urbanNames;
	
	@XmlElement(name = "regionsIDs")
	public List<String> regionsIDs;
	
	@XmlElement(name = "stopsAgencies")
	public List<String> stopsAgencies;
	
	@XmlElement(name = "stopsAgenciesNames")
	public List<String> stopsAgenciesNames;
	
	@XmlElement(name = "stopsIDs")
	public List<String> stopsIDs;
	
	@XmlElement(name = "stopsNames")
	public List<String> stopsNames;
	
	@XmlElement(name = "stopsLats")
	public List<Double> stopsLats;
	
	@XmlElement(name = "stopsLons")
	public List<Double> stopsLons;
	
	@XmlElement(name = "stopsVisits")
	public List<Integer> stopsVisits = new ArrayList<Integer>();
	
	@XmlElement(name = "routesAgencies")
	public List<String> routesAgencies;
	
	@XmlElement(name = "routesAgenciesNames")
	public List<String> routesAgenciesNames;
	
	@XmlElement(name = "routesIDs")
	public List<String> routesIDs;
	
	@XmlElement(name = "routeShortnames")
	public List<String> routeShortnames;
	
	@XmlElement(name = "routesLongnames")
	public List<String> routesLongnames;
	
	@XmlElement(name = "pnrIDs")
	public List<Integer> pnrIDs;
	
	@XmlElement(name = "pnrNames")
	public List<String> pnrNames;
	
	@XmlElement(name = "pnrCities")
	public List<String> pnrCities;
	
	@XmlElement(name = "pnrLats")
	public List<Double> pnrLats;
	
	@XmlElement(name = "pnrLons")
	public List<Double> pnrLons;
	
	@XmlElement(name = "pnrSpaces")
	public List<Integer> pnrSpaces;
	
	@XmlElement(name = "placesIDs")
	public List<String> placesIDs;
	
	@XmlElement(name = "placesNames")
	public List<String> placesNames;
	
}
