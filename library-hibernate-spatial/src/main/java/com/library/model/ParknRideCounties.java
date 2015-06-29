package com.library.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@XmlRootElement(name = "ParknRideCounty")
public class ParknRideCounties {
	
	@XmlAttribute
	@JsonSerialize
	public String countyId;
	
	@XmlAttribute
	@JsonSerialize
	public String cname;
	
	@XmlAttribute
	@JsonSerialize
	public String count; // Total number of P&R lots in the county
	
	@XmlAttribute
	@JsonSerialize
	public String spaces; // Total number of spaces is the county
	
	@XmlAttribute
	@JsonSerialize
	public String accessibleSpaces; // Total number of accessible sapces in the
									// county

	/*public ParknRideCounties() {
	}

	public ParknRideCounties(ParknRideCounties other) {
		this.countyId = other.countyId;
		this.cname = other.cname;
		this.count = other.count;
		this.spaces = other.spaces;
		this.accessibleSpaces = other.accessibleSpaces;
	}
	
	public void setCountyId(int i){
		this.countyId=i;
	}
	
	public void setCname(String s){
		this.cname=s;
	}
	
	public void setCount(int i){
		this.count=i;
	}
	
	public void setSpaces(int i){
		this.spaces=i;
	}
	
	public void setAccessibleSpaces(int i){
		this.accessibleSpaces=i;
	}
	
	public int getCountyId(){
		return this.countyId;
	}
	
	public String getCname(){
		return this.cname;
	}
	
	public int getCount(){
		return this.count;
	}
	
	public int getSpaces(){
		return this.spaces;
	}
	
	public int getAccessibleSpaces(){
		return this.accessibleSpaces;
	}*/
}
