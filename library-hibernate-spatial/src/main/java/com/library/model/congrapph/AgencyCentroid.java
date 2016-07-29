package com.library.model.congrapph;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@XmlRootElement( name = "AgencyCentroid")
public class AgencyCentroid {
	@XmlAttribute
	@JsonSerialize
	public String id;
	
	@XmlAttribute
	@JsonSerialize
	public double lat;
	
	@XmlAttribute
	@JsonSerialize
	public double lng;
}
