package com.library.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "ConGraphAgencyCentroids")
public class ConGraphAgencyCentroids {
	
	@XmlElement( name = "key")
	public String key;
	
	@XmlElement( name = "coordinates")
	public List<Coordinate> coordinates = new ArrayList<Coordinate>(); 
	
}
